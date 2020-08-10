/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.brentvatne.exoplayer;

import android.annotation.TargetApi;
import android.net.Uri;

import androidx.annotation.Nullable;

import android.text.TextUtils;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.drm.ExoMediaDrm.KeyRequest;
import com.google.android.exoplayer2.drm.ExoMediaDrm.ProvisionRequest;
import com.google.android.exoplayer2.upstream.DataSourceInputStream;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.source.dash.manifest.DashManifest;
import com.google.android.exoplayer2.source.dash.DashUtil;
import com.google.android.exoplayer2.drm.DrmInitData;
import com.google.android.exoplayer2.drm.DrmInitData.SchemeData;
import com.google.android.exoplayer2.upstream.HttpDataSource.InvalidResponseCodeException;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.android.exoplayer2.drm.MediaDrmCallback;

import android.util.Log;
import android.util.Base64;

import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import com.nexguard.quickmark.QuickMarkView;

/**
 * A {@link MediaDrmCallback} that makes requests using {@link HttpDataSource} instances.
 */
@TargetApi(18)
public final class WidevineMediaDrmCallback implements MediaDrmCallback {

    private static final int MAX_MANUAL_REDIRECTS = 5;

    private final HttpDataSource.Factory dataSourceFactory;
    private final String defaultLicenseUrl;
    private final boolean forceDefaultLicenseUrl;
    private final Map<String, String> keyRequestProperties;

    private String azamToken = null;
    private String drmAuthTokenURL = null;
    private Uri srcUri;
    private VideoEventEmitter eventEmitter;
    private QuickMarkView quickMarkView = null;

    /**
     * @param defaultLicenseUrl The default license URL. Used for key requests that do not specify
     *                          their own license URL.
     * @param dataSourceFactory A factory from which to obtain {@link HttpDataSource} instances.
     */
    public WidevineMediaDrmCallback(String defaultLicenseUrl, HttpDataSource.Factory dataSourceFactory) {
        this(defaultLicenseUrl, false, dataSourceFactory);
    }

    public WidevineMediaDrmCallback(String defaultLicenseUrl, HttpDataSource.Factory dataSourceFactory, Uri srcUri, String azamToken, String drmAuthTokenURL) {
        this(defaultLicenseUrl, false, dataSourceFactory);

        this.azamToken = azamToken;
        this.drmAuthTokenURL = drmAuthTokenURL;
        this.srcUri = srcUri;
    }

    public WidevineMediaDrmCallback(String defaultLicenseUrl, HttpDataSource.Factory dataSourceFactory, Uri srcUri, String azamToken, String drmAuthTokenURL, VideoEventEmitter eventEmitter) {
        this(defaultLicenseUrl, false, dataSourceFactory);

        this.azamToken = azamToken;
        this.drmAuthTokenURL = drmAuthTokenURL;
        this.srcUri = srcUri;
        this.eventEmitter = eventEmitter;
    }

    public WidevineMediaDrmCallback(String defaultLicenseUrl, HttpDataSource.Factory dataSourceFactory, Uri srcUri, String azamToken, String drmAuthTokenURL, VideoEventEmitter eventEmitter, QuickMarkView quickMarkView) {
        this(defaultLicenseUrl, false, dataSourceFactory);

        this.azamToken = azamToken;
        this.drmAuthTokenURL = drmAuthTokenURL;
        this.srcUri = srcUri;
        this.eventEmitter = eventEmitter;
        this.quickMarkView = quickMarkView;
    }

    /**
     * @param defaultLicenseUrl      The default license URL. Used for key requests that do not specify
     *                               their own license URL, or for all key requests if {@code forceDefaultLicenseUrl} is
     *                               set to true.
     * @param forceDefaultLicenseUrl Whether to use {@code defaultLicenseUrl} for key requests that
     *                               include their own license URL.
     * @param dataSourceFactory      A factory from which to obtain {@link HttpDataSource} instances.
     */
    public WidevineMediaDrmCallback(String defaultLicenseUrl, boolean forceDefaultLicenseUrl,
                                    HttpDataSource.Factory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
        this.defaultLicenseUrl = defaultLicenseUrl;
        this.forceDefaultLicenseUrl = forceDefaultLicenseUrl;
        this.keyRequestProperties = new HashMap<>();
    }

    /**
     * Sets a header for key requests made by the callback.
     *
     * @param name  The name of the header field.
     * @param value The value of the field.
     */
    public void setKeyRequestProperty(String name, String value) {
        Assertions.checkNotNull(name);
        Assertions.checkNotNull(value);
        synchronized (keyRequestProperties) {
            keyRequestProperties.put(name, value);
        }
    }

    /**
     * Clears a header for key requests made by the callback.
     *
     * @param name The name of the header field.
     */
    public void clearKeyRequestProperty(String name) {
        Assertions.checkNotNull(name);
        synchronized (keyRequestProperties) {
            keyRequestProperties.remove(name);
        }
    }

    /**
     * Clears all headers for key requests made by the callback.
     */
    public void clearAllKeyRequestProperties() {
        synchronized (keyRequestProperties) {
            keyRequestProperties.clear();
        }
    }

    @Override
    public byte[] executeProvisionRequest(UUID uuid, ProvisionRequest request) throws IOException {
//        Log.e("ReactExoplayerView","=========executeProvisionRequest: " + Util.fromUtf8Bytes(request.getData()));

        String url =
                request.getDefaultUrl() + "&signedRequest=" + Util.fromUtf8Bytes(request.getData());
        return executePost(dataSourceFactory, url, Util.EMPTY_BYTE_ARRAY, null);
    }

    @Override
    public byte[] executeKeyRequest(UUID uuid, KeyRequest request) throws Exception {

        String url = request.getLicenseServerUrl();
        if (forceDefaultLicenseUrl || TextUtils.isEmpty(url)) {
            url = defaultLicenseUrl;
        }
        Map<String, String> requestProperties = new HashMap<>();
        // Add standard request properties for supported schemes.
        String contentType = C.PLAYREADY_UUID.equals(uuid) ? "text/xml"
                : (C.CLEARKEY_UUID.equals(uuid) ? "application/json" : "application/octet-stream");
        requestProperties.put("Content-Type", contentType);
        if (C.PLAYREADY_UUID.equals(uuid)) {
            requestProperties.put("SOAPAction",
                    "http://schemas.microsoft.com/DRM/2007/03/protocols/AcquireLicense");
        }
        // Add additional request properties.
        synchronized (keyRequestProperties) {
            requestProperties.putAll(keyRequestProperties);
        }

        HttpDataSource dataSource = dataSourceFactory.createDataSource();
        DashManifest dashManifest = DashUtil.loadManifest(dataSource, this.srcUri);
        DrmInitData drmInitData = DashUtil.loadDrmInitData(dataSource, dashManifest.getPeriod(0));
        if (drmInitData == null) {
            return null;
        }

        SchemeData scheme = drmInitData.get(C.WIDEVINE_UUID);
        String schemeStr = Util.fromUtf8Bytes(scheme.data);
        String[] parts = schemeStr.split("\n");
        String contentId = parts[parts.length - 1];

        String pssh = Base64.encodeToString(scheme.data, Base64.NO_WRAP);
//        eventEmitter.drmKeysAcquired(contentId, pssh);

        //------ fetch Nagra token
        Map<String, String> params = new HashMap<>();
        params.put("Authorization", "Bearer " + this.azamToken);
        byte[] data = this.executeGet(this.drmAuthTokenURL + "?contentId=" + contentId, params);

        try {
            JSONObject jsonObject = new JSONObject(new String(data));

            if (!jsonObject.isNull("success")) {
                boolean success = jsonObject.getBoolean("success");

                if (success && !jsonObject.isNull("data")) {
                    JSONObject dataObj = jsonObject.getJSONObject("data");

                    if (!dataObj.isNull("token")) {
                        String token = dataObj.getString("token");

                        //set token on watermark view, if available
                        if (this.quickMarkView != null){
                            this.quickMarkView.setToken(token);
                        }

                        requestProperties.put("nv-authorizations", token);
                    }
                }
            }

        } catch (JSONException ex) {
            Log.d("DRM:", ex.toString());
        }


        //--- prepare req and response
        String challenge = Base64.encodeToString(request.getData(), Base64.NO_WRAP);
        JSONObject headers = new JSONObject(requestProperties);
        JSONObject nagraReq = new JSONObject();
        nagraReq.put("headers", headers);
        nagraReq.put("body", challenge);


//        return executePost(dataSourceFactory, url, request.getData(), requestProperties);
        byte[] responseData = executePost(dataSourceFactory, url, request.getData(), requestProperties);
        if(responseData != null){
            String nagraResponse = Base64.encodeToString(responseData, Base64.NO_WRAP);
            JSONObject nagraAll = new JSONObject();
            nagraAll.put("request", nagraReq);
            nagraAll.put("response", nagraResponse);
            eventEmitter.drmKeysAcquired(contentId, pssh, nagraAll.toString());
        }
        return  responseData;
    }

    private static byte[] executePost(
            HttpDataSource.Factory dataSourceFactory,
            String url,
            byte[] data,
            @Nullable Map<String, String> requestProperties)
            throws IOException {
        HttpDataSource dataSource = dataSourceFactory.createDataSource();
        if (requestProperties != null) {
            for (Map.Entry<String, String> requestProperty : requestProperties.entrySet()) {
                dataSource.setRequestProperty(requestProperty.getKey(), requestProperty.getValue());
            }
        }

        int manualRedirectCount = 0;
        while (true) {
            DataSpec dataSpec =
                    new DataSpec(
                            Uri.parse(url),
                            data,
                            /* absoluteStreamPosition= */ 0,
                            /* position= */ 0,
                            /* length= */ C.LENGTH_UNSET,
                            /* key= */ null,
                            DataSpec.FLAG_ALLOW_GZIP);
            DataSourceInputStream inputStream = new DataSourceInputStream(dataSource, dataSpec);
            try {
                return Util.toByteArray(inputStream);
            } catch (InvalidResponseCodeException e) {
                // For POST requests, the underlying network stack will not normally follow 307 or 308
                // redirects automatically. Do so manually here.
                boolean manuallyRedirect =
                        (e.responseCode == 307 || e.responseCode == 308)
                                && manualRedirectCount++ < MAX_MANUAL_REDIRECTS;
                String redirectUrl = manuallyRedirect ? getRedirectUrl(e) : null;
                if (redirectUrl == null) {
                    throw e;
                }
                url = redirectUrl;
            } finally {
                Util.closeQuietly(inputStream);
            }
        }
    }

    private static @Nullable
    String getRedirectUrl(InvalidResponseCodeException exception) {
        Map<String, List<String>> headerFields = exception.headerFields;
        if (headerFields != null) {
            List<String> locationHeaders = headerFields.get("Location");
            if (locationHeaders != null && !locationHeaders.isEmpty()) {
                return locationHeaders.get(0);
            }
        }
        return null;
    }

    public static byte[] executeGet(String url, Map<String, String> requestProperties)
            throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            if (requestProperties != null) {
                for (Map.Entry<String, String> requestProperty : requestProperties.entrySet()) {
                    urlConnection.setRequestProperty(requestProperty.getKey(), requestProperty.getValue());
                }
            }

            // Read and return the response body.
            InputStream inputStream = urlConnection.getInputStream();
            try {
                return Util.toByteArray(inputStream);
            } finally {
                inputStream.close();
            }
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

}
