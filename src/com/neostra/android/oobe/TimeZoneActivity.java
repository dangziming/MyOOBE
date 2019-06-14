package com.neostra.android.oobe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.neostra.android.oobe.helper.Define;
import com.neostra.android.oobe.helper.ZoneGetter;
import com.neostra.android.oobe.wizard.WizardManager;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.text.BidiFormatter;
import android.text.TextDirectionHeuristics;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.setupwizardlib.util.SystemBarHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParserException;

public class TimeZoneActivity extends Activity implements OnItemClickListener {
    
    private static final String TAG = Define.getTag(TimeZoneActivity.class);
    private static final String XMLTAG_TIMEZONE = "timezone";
    
    private ListView zone_lv;
    // private LinearLayout time_zone_layout;
    // private FragmentManager manager;
    // private FragmentTransaction transaction;
    private ZoneAdapter adapter;
    private List<ZoneBody> list = new ArrayList<>();

    private List<Map<String, Object>> zoneNameList;
    private int zoneCount;
    private String[] zoneID;
    private String[] zoneName;
    private String[] zoneText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_time_zone);
        
		findViews();
		setLiteners();
        initList();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
        SystemBarHelper.hideSystemBars(getWindow());
    }

    public void goNext(View v) {
        Log.d(TAG, "goNext");

        Intent i = WizardManager.getNextWizardIntent(getIntent(), RESULT_OK);
        startActivity(i);
    }

    public void goBack(View v) {
        Log.d(TAG, "goBack");
        finish();
    }

	private void findViews() {
        // time_zone_layout = (LinearLayout) findViewById(R.id.time_zone_layout);
        // manager = getFragmentManager();
        // ZonePicker zonePicker = new ZonePicker();
        // transaction = manager.beginTransaction();
        // transaction.add(R.id.time_zone_layout, zonePicker, "ZonePicker");
        // transaction.commit();

        zone_lv = (ListView) findViewById(R.id.time_zone_lv);
        // zone_lv.setDividerHeight(0);

		adapter = new ZoneAdapter(list);
		zone_lv.setAdapter(adapter);
	}

	private void setLiteners() {
		zone_lv.setOnItemClickListener(this);
	}

	public void initList() {

        zoneNameList = ZoneGetter.getZonesList(this);
        int selectedZone = -1;
        String defaultZoneID = TimeZone.getDefault().getID();
        zoneCount = zoneNameList.size();
        zoneID = new String[zoneCount];
        zoneName = new String[zoneCount];
        zoneText = new String[zoneCount];
        for (int i = 0; i < zoneCount; i++) {
            Map<?, ?> map = zoneNameList.get(i);
            zoneID[i] = (String) map.get(ZoneGetter.KEY_ID);
            zoneName[i] = (String) map.get(ZoneGetter.KEY_DISPLAYNAME);
            zoneText[i] = (String) map.get(ZoneGetter.KEY_GMT);
            if (zoneID[i].equals(defaultZoneID)) {
                selectedZone = i;
            }
        }
        refreshList(selectedZone);
        zone_lv.setSelection(selectedZone);

    }

    public void refreshList(int id) {
        list.clear();
        for (int i = 0; i < zoneCount; i++) {
            ZoneBody zb = new ZoneBody();
            zb.setName(zoneName[i]);
            zb.setText(zoneText[i]);
            if (id == i) {
                zb.setSelected(true);
            } else {
                zb.setSelected(false);
            }
            list.add(zb);
            adapter.notifyDataSetChanged();
        }
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick, position: " + position);
        String tzId = (String)zoneNameList.get(position).get(ZoneGetter.KEY_ID);
        // Update the system timezone value
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.setTimeZone(tzId);
        refreshList(position);
	}

//  ----------------------------------

    public class ZoneBody {
        private String ID;
        private String displayName;
        private String gmtOffsetText;
        private boolean selected;

        public String getID(){return ID;}
        public void setID(String ID){this.ID=ID;}
        public String getName(){return displayName;}
        public void setName(String displayName){this.displayName=displayName;}
        public String getText(){return gmtOffsetText;}
        public void setText(String gmtOffsetText){this.gmtOffsetText=gmtOffsetText;}
        public boolean isSelected(){return selected;}
        public void setSelected(boolean flag){this.selected=flag;}
    }

    public class ZoneAdapter extends BaseAdapter {

        private class ViewHolder {
            private TextView zone_name;
            private TextView zone_value;
            private ImageView selected_view;
        }

        private List<ZoneBody> list;

        public ZoneAdapter(List<ZoneBody> list) {
            this.list = list;
        }
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if(convertView == null){
                convertView = View.inflate(parent.getContext(), R.layout.zone_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.zone_name = (TextView) convertView.findViewById(R.id.zone_name);
                viewHolder.zone_value = (TextView) convertView.findViewById(R.id.zone_value);
                viewHolder.selected_view = (ImageView) convertView.findViewById(R.id.selected);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.zone_name.setText(list.get(position).getName());
            viewHolder.zone_value.setText(list.get(position).getText());
            if (list.get(position).isSelected()) {
                viewHolder.selected_view.setVisibility(View.VISIBLE);
            } else {
                viewHolder.selected_view.setVisibility(View.INVISIBLE);
            }
            
            return convertView;
        }

    }

}
