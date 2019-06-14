package com.neostra.android.oobe;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.neostra.android.oobe.helper.Define;
import com.neostra.android.oobe.wizard.WizardManager;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.view.Window;
import android.view.WindowManager;

import com.android.internal.app.LocalePicker;
import com.android.internal.app.LocalePicker.LocaleInfo;

import com.android.setupwizardlib.util.SystemBarHelper;

public class LanguageActivity extends Activity implements OnItemClickListener {
    
    private static final String TAG = Define.getTag(LanguageActivity.class);

    private static String LANGUAGE_NAME[] = new String[] {"English","简体中文","繁體中文","ไทย","Melayu"};
    private static String LANGUAGE_GET_VALUE[] = new String[] {"English (United States)","中文 (简体中文,中国)","中文 (繁體中文,台灣)","ไทย (ไทย)","Bahasa Melayu (Malaysia)"};
    private static String LANGUAGE_SET_VALUE[] = new String[] {"English (United States)","中文 (简体中文,中国)","中文 (繁體中文,台灣)","ไทย","Bahasa Melayu (Malaysia)"};    //frameworks/base/core/res/res/values/locale_config.xml
    
    private ListView language_lv;
    private TextView title_tv;
    private Button next_bt;
	private Locale selectedLocale;
    private LanguageAdapter adapter;
    private List<LanguageBody> list = new ArrayList<>();
    private ArrayAdapter<LocaleInfo> googleLanguageArrayAdapter;
    private String[] languageItemString;
    private int selectedItem = -1;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN , WindowManager.LayoutParams.FLAG_FULLSCREEN); 
        setContentView(R.layout.activity_language);
        
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
        if (selectedItem!=-1)LocalePicker.updateLocale(selectedLocale);
        Log.d(TAG, "goNext");

        Intent i = WizardManager.getNextWizardIntent(getIntent(), RESULT_OK);
        startActivity(i);
    }

	private void findViews() {
        language_lv = (ListView) findViewById(R.id.language_lv);
        //language_lv.setDividerHeight(0);
        title_tv = (TextView) findViewById(R.id.language_title);
		next_bt = (Button) findViewById(R.id.nextBtn);

		adapter = new LanguageAdapter(list);
		language_lv.setAdapter(adapter);
	}

	private void setLiteners() {
		language_lv.setOnItemClickListener(this);
	}

	public void initList() {

        googleLanguageArrayAdapter = LocalePicker.constructAdapter(this);
        Locale defaultLocale = Locale.getDefault();
        String defaultLocaleName = Locale.getDefault().getDisplayName();
        languageItemString = new String[googleLanguageArrayAdapter.getCount()];    //将系统的语言列表提取出来
        int index = -1;//Arrays.binarySearch(LANGUAGE_GET_VALUE, defaultLocaleName);
        for (int i=0; i < LANGUAGE_GET_VALUE.length; i++) {
            if (LANGUAGE_GET_VALUE[i].equals(defaultLocaleName)) {
                index = i;
            }
        }
        Log.d(TAG, "defaultLocaleName: " + defaultLocaleName + ", index: " + index);

        int selectIndex = -1;
        for (int i = 0; i < googleLanguageArrayAdapter.getCount(); i++) {
            if (index != -1 && LANGUAGE_SET_VALUE[index].equals(((LocalePicker.LocaleInfo) googleLanguageArrayAdapter.getItem(i)).toString())) {
                selectIndex = i;
            }
            languageItemString[i] = googleLanguageArrayAdapter.getItem(i).toString();
            Log.d(TAG, "languageItemString[ " + i + " ]: " + languageItemString[i]);
        }
        String selectLanguage = "";
        if (selectIndex != -1) {
            selectLanguage = googleLanguageArrayAdapter.getItem(selectIndex).toString();
        }
        Log.d(TAG, "selectIndex: " + selectIndex + ", selectLanguage: " + selectLanguage);
        refreshList(selectLanguage);

    }

    public void refreshList(String sr) {
        list.clear();
        for (int i = 0; i < LANGUAGE_NAME.length; i++) {
            LanguageBody lb = new LanguageBody();
            lb.setLanguage(LANGUAGE_NAME[i]);
            // Log.d(TAG, "LANGUAGE_ITEM[i]: " + LANGUAGE_ITEM[i]);
            if (sr.equals(LANGUAGE_SET_VALUE[i])) {
                lb.setSelected(true);
            } else {
                lb.setSelected(false);
            }
            list.add(lb);
            adapter.notifyDataSetChanged();
        }
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick, position: " + position);
        Resources localResources = getBaseContext().getResources();
        Configuration localConfiguration = new Configuration(localResources.getConfiguration());
        
        for (int i = 0; i < languageItemString.length; i++) {
            if(languageItemString[i].equals(LANGUAGE_SET_VALUE[position])) {
                selectedItem = i;
                Locale locale = googleLanguageArrayAdapter.getItem(selectedItem).getLocale();
                selectedLocale = locale;
                localConfiguration.setLocale(locale);
                localConfiguration.setLayoutDirection(locale);
                localResources.updateConfiguration(localConfiguration, null);

                String selectedLanguage = googleLanguageArrayAdapter.getItem(selectedItem).toString();
                Log.d(TAG, "selectedLanguage: " + selectedLanguage);
                refreshList(selectedLanguage);

                title_tv.setText(R.string.language_panel_text);
                next_bt.setText(R.string.next_button_text);
                return;
            }
        }
	}

//  ----------------------------------

    public class LanguageBody {
        private String language;
        private boolean selected;
        public String getLanguage(){return language;}
        public void setLanguage(String text){this.language=text;}
        public boolean isSelected(){return selected;}
        public void setSelected(boolean flag){this.selected=flag;}
    }

    public class LanguageAdapter extends BaseAdapter {

        private class ViewHolder {
            private TextView language_view;
            private ImageView selected_view;
        }

        private List<LanguageBody> list;

        public LanguageAdapter(List<LanguageBody> list) {
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
                convertView = View.inflate(parent.getContext(), R.layout.language_list_item, null);
                viewHolder = new ViewHolder();
                viewHolder.language_view = (TextView) convertView.findViewById(R.id.language);
                viewHolder.selected_view = (ImageView) convertView.findViewById(R.id.selected);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.language_view.setText(list.get(position).getLanguage());
            if (list.get(position).isSelected()) {
                viewHolder.selected_view.setVisibility(View.VISIBLE);
            } else {
                viewHolder.selected_view.setVisibility(View.INVISIBLE);
            }
            
            return convertView;
        }

    }

}
