package me.chanjar.weixin.cp.api.impl;

import com.google.gson.JsonObject;
import me.chanjar.weixin.common.bean.WxAccessToken;
import me.chanjar.weixin.common.enums.WxType;
import me.chanjar.weixin.common.error.WxError;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.error.WxRuntimeException;
import me.chanjar.weixin.common.util.json.GsonParser;
import me.chanjar.weixin.cp.config.WxCpConfigStorage;
import me.chanjar.weixin.cp.constant.WxCpApiPathConsts;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.util.concurrent.locks.Lock;

import static me.chanjar.weixin.cp.constant.WxCpApiPathConsts.GET_AGENT_CONFIG_TICKET;
import static me.chanjar.weixin.cp.constant.WxCpApiPathConsts.GET_JSAPI_TICKET;

/**
 *
 *根据accessToken url更新accessToken
 *
 */
public class WxCpServiceCustomImpl extends WxCpServiceImpl {
    @Override
    public String getAccessToken(boolean forceRefresh) throws WxErrorException {
        final WxCpConfigStorage configStorage = getWxCpConfigStorage();
        if (!configStorage.isAccessTokenExpired() && !forceRefresh) {
            return configStorage.getAccessToken();
        }
        Lock lock = configStorage.getAccessTokenLock();
        lock.lock();
        try {
            // 拿到锁之后，再次判断一下最新的token是否过期，避免重刷
            if (!configStorage.isAccessTokenExpired() && !forceRefresh) {
                return configStorage.getAccessToken();
            }
            String url = String.format(configStorage.getApiUrl(WxCpApiPathConsts.GET_TOKEN),
                    this.configStorage.getCorpId(), this.configStorage.getCorpSecret());
            try {
                HttpGet httpGet = new HttpGet(url);
                if (getRequestHttpProxy() != null) {
                    RequestConfig config = RequestConfig.custom().setProxy(getRequestHttpProxy()).build();
                    httpGet.setConfig(config);
                }
                String resultContent;
                try (CloseableHttpClient httpClient = getRequestHttpClient();
                     CloseableHttpResponse response = httpClient.execute(httpGet)) {
                    resultContent = new BasicResponseHandler().handleResponse(response);
                } finally {
                    httpGet.releaseConnection();
                }
                WxError error = WxError.fromJson(resultContent, WxType.CP);
                if (error.getErrorCode() != 0) {
                    throw new WxErrorException(error);
                }

                WxAccessToken accessToken = WxAccessToken.fromJson(resultContent);
                configStorage.updateAccessToken(accessToken.getAccessToken(), accessToken.getExpiresIn());
            } catch (IOException e) {
                throw new WxRuntimeException(e);
            }
        } finally {
            lock.unlock();
        }
        return configStorage.getAccessToken();
    }
}
