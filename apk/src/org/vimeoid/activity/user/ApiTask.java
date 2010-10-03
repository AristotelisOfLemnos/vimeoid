/**
 * 
 */
package org.vimeoid.activity.user;

import org.json.JSONException;
import org.json.JSONObject;
import org.vimeoid.connection.VimeoApi;
import org.vimeoid.connection.VimeoApi.AdvancedApiCallError;
import org.vimeoid.util.ApiParams;

import android.os.AsyncTask;
import android.util.Log;

/**
 * <dl>
 * <dt>Project:</dt> <dd>vimeoid</dd>
 * <dt>Package:</dt> <dd>org.vimeoid.activity.user</dd>
 * </dl>
 *
 * <code>ApiTask</code>
 *
 * <p>Description</p>
 *
 * @author Ulric Wilfred <shaman.sir@gmail.com>
 * @date Oct 1, 2010 8:55:58 PM 
 *
 */
public abstract class ApiTask extends AsyncTask<ApiParams, Void, JSONObject> {
    
    private static final String TAG = "ApiTask";
    
    protected final String apiMethod;
    
    protected boolean queryRunning;
    
    protected ApiTask(String apiMethod) {
        this.apiMethod = apiMethod;
    }
    
    @Override
    protected JSONObject doInBackground(ApiParams... paramsList) {
        final ApiParams params = prepareParams(paramsList);
        try {
            ensureConnected();
            queryRunning = true;
            if (params == null || params.isEmpty()) {
                return VimeoApi.advancedApi(apiMethod);
            } else {
                return VimeoApi.advancedApi(apiMethod, params);
            }
        } catch (AdvancedApiCallError aace) { onApiError(aace); } 
          catch (Exception e) { onException(params, e); }
        
        return null;
    }
    
    protected ApiParams prepareParams(ApiParams... paramsList) {
        if (paramsList.length <= 0) return null;
        if (paramsList.length > 1) throw new UnsupportedOperationException("This task do not supports several params lists");
        
        return paramsList[0];
    }
    
    @Override
    protected void onPostExecute(JSONObject jsonObj) {
        // Log.d(TAG, jsonObj.toString());
        if (jsonObj != null) {
            try {
                queryRunning = false;                
                onAnswerReceived(jsonObj);
            } catch (JSONException jsone) {
                onJsonParsingError(jsone);
            }
        } else { onNullReturned(); }
    }
    
    protected void onAnyError(Exception e, String message) {
        Log.e(TAG, message);
    }
    
    protected void onApiError(AdvancedApiCallError error) {
        onAnyError(error, "API Error " + error.code + " / " + error.message);
    }
    
    protected void onNullReturned() {
        onAnyError(null, "Failed to receive object " + apiMethod);
    }
    
    protected void onJsonParsingError(JSONException jsone) {
        onAnyError(jsone, "JSON parsing failure: " + jsone.getLocalizedMessage());
    }
    
    protected void onException(ApiParams params, Exception e) {
        onAnyError(e, "Error while calling " + apiMethod + " " + params + " " + e.getLocalizedMessage());        
    }
    
    protected void ensureConnected() {
        return; // FIXME: not used
    }
    
    /* protected boolean checkConnection() {
        if (VimeoApi.connectedToWeb(this) && VimeoApi.vimeoSiteReachable(this)) {
            Log.d(TAG, "Connection test is passed OK");
            queryMoreItems(adapter, pageNum);
        } else {
            Log.d(TAG, "Connection test failed");            
            Dialogs.makeToast(this, getString(R.string.no_iternet_connection));
        }
    } */
    
    public abstract void onAnswerReceived(JSONObject jsonObj) throws JSONException;
    
    public boolean queryRunning() { return queryRunning; } 

}