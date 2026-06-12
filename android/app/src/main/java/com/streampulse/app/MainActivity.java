package com.streampulse.app;

import android.os.Bundle;
import android.webkit.WebView;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        WebView webView = this.bridge.getWebView();
        if (webView != null) {
            LeeStreamTVBridge bridgeInstance = new LeeStreamTVBridge(this);
            webView.addJavascriptInterface(bridgeInstance, "LeeStreamTVBridge");
            webView.addJavascriptInterface(bridgeInstance, "LeePrimeBridge");
        }
    }
}
