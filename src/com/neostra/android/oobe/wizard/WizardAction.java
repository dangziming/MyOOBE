package com.neostra.android.oobe.wizard;

import org.xmlpull.v1.XmlPullParser;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

public class WizardAction {
    private static final String TAG              = WizardAction.class.getSimpleName();
    private static final String ATTR_ID          = "id";
    private static final String ATTR_URI         = "uri";
    private static final String ATTR_ACTION      = "action";
    private static final String ATTR_RESULT_CODE = "resultCode";

    private static final String TAG_RESULT       = "result";

    public String actionId;
    public String uri;

    public SparseArray<String> resultArr;
    public String defaultAction;

    public WizardAction(XmlPullParser xrp) throws Exception {
        actionId = xrp.getAttributeValue(null, ATTR_ID);
        uri      = xrp.getAttributeValue(WizardParser.NAMESPACE_WIZARD, ATTR_URI);

        if (TextUtils.isEmpty(actionId) || TextUtils.isEmpty(uri)) {
            throw new Exception("WizardAction actionId(" + actionId + ") or uri(" + uri + ") empty!!");
        }
        xrp.next();

        Log.d(TAG, "START WizardAction actionId(" + actionId + "), uri(" + uri + ")");
        // parse result
        resultArr = new SparseArray<String>();
        while (xrp.getEventType() != XmlPullParser.END_TAG || !WizardParser.TAG_WIZARD_ACTION.equals(xrp.getName())) {
            if (xrp.getEventType() == XmlPullParser.START_TAG && TAG_RESULT.equals(xrp.getName())) {
                parseResult(xrp);
            }
            xrp.next();
        }
        Log.d(TAG, "END WizardAction actionId(" + actionId + ")");
    }

    private void parseResult(XmlPullParser xrp) throws Exception {
        String actionId = xrp.getAttributeValue(WizardParser.NAMESPACE_WIZARD, ATTR_ACTION);
        String rcString = xrp.getAttributeValue(WizardParser.NAMESPACE_WIZARD, ATTR_RESULT_CODE);
        int resultCode;

        if (TextUtils.isEmpty(actionId))
            throw new Exception("Result action is empty");

        if (TextUtils.isEmpty(rcString)) {
            defaultAction = actionId;
            Log.d(TAG, "RESULT defaultAction: " + defaultAction);
        } else {
            try {
                resultCode = Integer.parseInt(rcString);
                resultArr.put(resultCode, actionId);
                Log.d(TAG, "RESULT resultCode: " + resultCode + ", action: " + actionId);
            } catch (Exception e) {
                throw new Exception("Result resultCode cannot parse to int", e);
            }
        }
    }
}