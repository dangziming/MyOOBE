package com.neostra.android.oobe.wizard;

import java.io.InputStream;
import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;

import com.neostra.android.oobe.R;
import com.neostra.android.oobe.helper.UserHelper;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

public class WizardParser {
    private static final String TAG       = WizardParser.class.getSimpleName();
    static final String NAMESPACE_WIZARD  = "http://schemas.android.com/apk/res/com.neostra.android.oobe";

    static final String TAG_WIZARD_SCRIPT = "WizardScript";
    static final String TAG_WIZARD_ACTION = "WizardAction";

    private final String ATTR_FIRST_ACTION = "firstAction";

    String firstAction;
    HashMap<String, WizardAction> map;

    public WizardParser(Context context) {
        Log.d(TAG, "START Wizard Parsing");
        try {
            int wizardScript = UserHelper.isOwnerUser(context) ? R.raw.wizard_script : R.raw.wizard_script_mu;
        	//int wizardScript = R.raw.wizard_script_mu;
            InputStream open = context.getResources().openRawResource(wizardScript);
            XmlPullParser xrp = Xml.newPullParser();

            xrp.setInput(open, null);

            // parse wizard script to get the first action
            firstAction = getFirstAction(xrp);
            Log.d(TAG, "FirstAction: " + firstAction);

            map = new HashMap<String, WizardAction>();
            while (xrp.getEventType() != XmlPullParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlPullParser.START_TAG && TAG_WIZARD_ACTION.equals(xrp.getName())) {
                    WizardAction wa = new WizardAction(xrp);
                    map.put(wa.actionId, wa);
                } else {
                    xrp.next();
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "WizardParser parsing err ", e);
        }
        Log.d(TAG, "END Wizard Parsing");
    }

    private String getFirstAction(XmlPullParser xrp) throws Exception {
        if (xrp.getEventType() == XmlPullParser.START_DOCUMENT)
            xrp.next();

        if (!TAG_WIZARD_SCRIPT.equals(xrp.getName()))
            throw new Exception("XML must start with <WizardScript> : " + xrp.getName());

        String firstAction = xrp.getAttributeValue(NAMESPACE_WIZARD, ATTR_FIRST_ACTION);
        if (TextUtils.isEmpty(firstAction))
            throw new Exception("WizardScript must define a firstAction");

        xrp.next();
        return firstAction;
    }
}