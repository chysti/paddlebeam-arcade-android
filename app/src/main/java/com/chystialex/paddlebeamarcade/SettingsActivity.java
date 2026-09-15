package com.chystialex.paddlebeamarcade;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

public final class SettingsActivity extends Activity {
    private SharedPreferences prefs;
    private LinearLayout box;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs=getSharedPreferences("scores",MODE_PRIVATE);
        buildScreen();
    }

    private void buildScreen(){
        boolean day=prefs.getBoolean("day_theme",false);
        int bg=day?0xffe0f2fe:0xff030712, fg=day?0xff0f172a:Color.WHITE, accent=day?0xff0369a1:0xff22d3ee;
        getWindow().setStatusBarColor(bg);getWindow().setNavigationBarColor(bg);
        box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(28),dp(48),dp(28),dp(36));box.setBackgroundColor(bg);
        I18n.direction(box,this);
        TextView title=text(I18n.t(this,"settings"),30,accent);title.setGravity(Gravity.CENTER);title.setTypeface(android.graphics.Typeface.MONOSPACE,android.graphics.Typeface.BOLD);box.addView(title,params(dp(28)));
        box.addView(text(I18n.t(this,"theme"),18,accent),params(dp(10)));
        RadioGroup themes=new RadioGroup(this);themes.setOrientation(RadioGroup.VERTICAL);
        RadioButton night=radio(I18n.t(this,"night"),fg,!day), daylight=radio(I18n.t(this,"day"),fg,day);themes.addView(night);themes.addView(daylight);box.addView(themes,params(dp(20)));
        themes.setOnCheckedChangeListener((g,id)->{boolean chooseDay=id==daylight.getId();prefs.edit().putBoolean("day_theme",chooseDay).apply();buildScreen();});
        Switch mute=new Switch(this);mute.setText(I18n.t(this,"mute"));mute.setTextSize(18);mute.setTextColor(fg);mute.setChecked(prefs.getBoolean("muted",false));mute.setPadding(0,dp(8),0,dp(8));mute.setOnCheckedChangeListener((v,on)->prefs.edit().putBoolean("muted",on).apply());box.addView(mute,params(dp(12)));
        TextView note=text(I18n.t(this,"mute_note"),14,day?0xff475569:0xff94a3b8);box.addView(note,params(dp(22)));
        box.addView(text(I18n.t(this,"language"),18,accent),params(dp(8)));
        RadioGroup languages=new RadioGroup(this);String current=I18n.selection(this);RadioButton system=radio(I18n.t(this,"system"),fg,"system".equals(current)),en=radio("English",fg,"en".equals(current)),he=radio("עברית",fg,"he".equals(current)),ru=radio("Русский",fg,"ru".equals(current));languages.addView(system);languages.addView(en);languages.addView(he);languages.addView(ru);box.addView(languages,params(dp(22)));
        languages.setOnCheckedChangeListener((g,id)->{String selected=id==system.getId()?"system":id==he.getId()?"he":id==ru.getId()?"ru":"en";prefs.edit().putString("language",selected).apply();buildScreen();});
        Button back=new Button(this);back.setText(I18n.t(this,"back"));back.setTextColor(Color.WHITE);back.setBackgroundColor(day?0xff0369a1:0xff0e7490);back.setOnClickListener(v->finish());box.addView(back,params(0));
        setContentView(box);
    }

    private RadioButton radio(String label,int color,boolean checked){RadioButton r=new RadioButton(this);r.setId(android.view.View.generateViewId());r.setText(label);r.setTextSize(18);r.setTextColor(color);r.setChecked(checked);return r;}
    private TextView text(String value,float size,int color){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);return t;}
    private LinearLayout.LayoutParams params(int bottom){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=bottom;return p;}
    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
}
