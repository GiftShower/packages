// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.inapppurchase;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.android.billingclient.api.BillingClient;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel;

/** Wraps a {@link BillingClient} instance and responds to Dart calls for it. */
public class InAppPurchasePlugin implements FlutterPlugin, ActivityAware {

  static final String PROXY_PACKAGE_KEY = "PROXY_PACKAGE";
  // The proxy value has to match the <package> value in library's AndroidManifest.xml.
  // This is important that the <package> is not changed, so we hard code the value here then having
  // a unit test to make sure. If there is a strong reason to change the <package> value, please inform the
  // code owner of this package.
  static final String PROXY_VALUE = "io.flutter.plugins.inapppurchase";

  private MethodChannel methodChannel;
  private MethodCallHandlerImpl methodCallHandler;

  /** Plugin registration. */
  @SuppressWarnings("deprecation")
  public static void registerWith(
      @NonNull io.flutter.plugin.common.PluginRegistry.Registrar registrar) {
    InAppPurchasePlugin plugin = new InAppPurchasePlugin();
    registrar.activity().getIntent().putExtra(PROXY_PACKAGE_KEY, PROXY_VALUE);
    ((Application) registrar.context().getApplicationContext())
        .registerActivityLifecycleCallbacks(plugin.methodCallHandler);
  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
    setUpMethodChannel(binding.getBinaryMessenger(), binding.getApplicationContext());
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPlugin.FlutterPluginBinding binding) {
    teardownMethodChannel();
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    binding.getActivity().getIntent().putExtra(PROXY_PACKAGE_KEY, PROXY_VALUE);
    methodCallHandler.setActivity(binding.getActivity());
  }

  @Override
  public void onDetachedFromActivity() {
    methodCallHandler.setActivity(null);
    methodCallHandler.onDetachedFromActivity();
  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
    onAttachedToActivity(binding);
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    methodCallHandler.setActivity(null);
  }

  private void setUpMethodChannel(BinaryMessenger messenger, Context context) {
    methodChannel = new MethodChannel(messenger, "plugins.flutter.io/in_app_purchase");
    methodCallHandler =
        new MethodCallHandlerImpl(
            /*activity=*/ null, context, methodChannel, new BillingClientFactoryImpl());
    methodChannel.setMethodCallHandler(methodCallHandler);
  }

  private void teardownMethodChannel() {
    methodChannel.setMethodCallHandler(null);
    methodChannel = null;
    methodCallHandler = null;
  }

  @VisibleForTesting
  void setMethodCallHandler(MethodCallHandlerImpl methodCallHandler) {
    this.methodCallHandler = methodCallHandler;
  }
}
