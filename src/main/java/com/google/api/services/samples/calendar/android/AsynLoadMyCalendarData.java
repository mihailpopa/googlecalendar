/*
 * Copyright (c) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.services.samples.calendar.android;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.extensions.android.accounts.GoogleAccountManager;
import com.google.api.client.googleapis.services.GoogleKeyInitializer;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.model.Calendar;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

/**
 * @author mihai@google.com (Your Name Here)
 *
 */
public class AsynLoadMyCalendarData extends AsyncTask<Void, Void, Void>{
  
  private static final String TAG = "AsyncLoadMyCalendar";
  
  private com.google.api.services.calendar.Calendar calendar;
  private ProgressDialog dialog;
  private Activity contextActivity;
  private GoogleAccountManager accountManager;
  private String authToken;
  private static final String AUTH_TOKEN_TYPE = "cl";
  static final String PREF_ACCOUNT_NAME = "accountName";
  private static final int REQUEST_AUTHENTICATE = 0;
  static final String PREF_AUTH_TOKEN = "authToken";
  private String accountName;
  SharedPreferences settings;
  final HttpTransport transport = AndroidHttp.newCompatibleTransport();
  final JsonFactory jsonFactory = new GsonFactory();
  
  AsynLoadMyCalendarData(Activity context) {
    accountManager = new GoogleAccountManager(context);
    this.contextActivity = context;
    settings = context.getPreferences(context.MODE_PRIVATE);

  }

  @Override
  protected Void doInBackground(Void... params) {
    HttpRequestInitializer requestInitializer = new HttpRequestInitializer() {
      public void initialize(HttpRequest request) throws IOException {
        Log.d(TAG, "Called the initializer of the request initializer object");
        request.getHeaders().setAuthorization(GoogleHeaders.getGoogleLoginValue(authToken));
      }
    };    
    
    calendar = new com.google.api.services.calendar.
    Calendar.Builder(transport, jsonFactory, requestInitializer)
      .setApplicationName("CalendarTEST")
      .setJsonHttpRequestInitializer(new GoogleKeyInitializer(ClientCredentials.KEY))
      .build();
    chooseAccount();

    Log.d(TAG, "I should get an auth token");
    
    return null;
  }
 
  
  private void chooseAccount() {
    accountManager.getAccountManager().getAuthTokenByFeatures(GoogleAccountManager.ACCOUNT_TYPE,
        AUTH_TOKEN_TYPE,
        null,
        contextActivity,
        null,
        null,
        new AccountManagerCallback<Bundle>() {

          public void run(AccountManagerFuture<Bundle> future) {
            Bundle bundle;
            try {
              bundle = future.getResult();
              setAccountName(bundle.getString(AccountManager.KEY_ACCOUNT_NAME));
              setAuthToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));
              onAuthToken();
            } catch (OperationCanceledException e) {
              // user canceled
            } catch (AuthenticatorException e) {
              Log.e(TAG, e.getMessage(), e);
            } catch (IOException e) {
              Log.e(TAG, e.getMessage(), e);
            }
          }
        },
        null);
  }
  void setAccountName(String accountName) {
    SharedPreferences.Editor editor = settings.edit();
    editor.putString(PREF_ACCOUNT_NAME, accountName);
    editor.commit();
    this.accountName = accountName;
  }

  void setAuthToken(String authToken) {
    SharedPreferences.Editor editor = settings.edit();
    editor.putString(PREF_AUTH_TOKEN, authToken);
    editor.commit();
    this.authToken = authToken;
  }
  
  void onAuthToken() {
      new Thread(new Runnable() {
        
        public void run() {
          Calendar cal;
          try {
            cal = calendar.calendars().get("mihail.popa@xoomworks.com").execute();
            Log.d(TAG, "Calendar   info: " + cal.toPrettyString());          

          } catch (IOException exception) {
            Log.e(TAG, "There was an exception accessing calendar data");
            exception.printStackTrace();
          }
        }
      }).start();
 
  }
}
