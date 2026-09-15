package com.chystialex.paddlebeamarcade;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.content.pm.PackageInfo;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class AboutActivity extends Activity {
    private static final int CYAN = Color.rgb(34, 211, 238);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(3,7,18));
        getWindow().setNavigationBarColor(Color.rgb(3,7,18));

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(3,7,18));
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(24), dp(36), dp(24), dp(36));
        scroll.addView(box);
        I18n.direction(box,this);

        TextView title = text(I18n.t(this,"title"), 28, CYAN);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD);
        box.addView(title, matchWrap(dp(18)));
        TextView version = text(I18n.t(this,"app_version")+"\nPaddleBeam Arcade " + appVersion(), 16, Color.WHITE);
        box.addView(version, matchWrap(dp(18)));
        box.addView(text("Developed by Chysti Alex\n© 2026 Chysti Alex", 17, Color.WHITE), matchWrap(dp(22)));

        box.addView(heading(I18n.t(this,"contact")), matchWrap(dp(8)));
        Button contact = button(I18n.t(this,"email"));
        contact.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:chysti75@gmail.com?subject=PaddleBeam%20Arcade"))));
        box.addView(contact, matchWrap(dp(22)));

        box.addView(heading(I18n.t(this,"privacy")), matchWrap(dp(8)));
        box.addView(text(I18n.t(this,"privacy_text"), 15, 0xffcbd5e1), matchWrap(dp(22)));

        Button support = button(I18n.t(this,"support"));
        support.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://paypal.me/Chysti75"))));
        box.addView(support, matchWrap(dp(12)));
        box.addView(text(I18n.t(this,"support_note")+"\n\nSupport is voluntary and does not unlock features, remove ads, or provide digital benefits.", 14, 0xff94a3b8), matchWrap(dp(24)));

        Button close = button(I18n.t(this,"back"));
        close.setOnClickListener(v -> finish());
        box.addView(close, matchWrap(0));
        setContentView(scroll);
    }

    private TextView heading(String s){ TextView v=text(s,19,CYAN); v.setTypeface(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD); return v; }
    private TextView text(String s,float size,int color){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setLineSpacing(0,1.18f);v.setMovementMethod(LinkMovementMethod.getInstance());return v;}
    private Button button(String s){Button b=new Button(this);b.setText(s);b.setTextColor(Color.WHITE);b.setBackgroundColor(0xff0e7490);b.setAllCaps(false);return b;}
    private String appVersion(){
        try {
            PackageInfo p=getPackageManager().getPackageInfo(getPackageName(),0);
            long code=android.os.Build.VERSION.SDK_INT>=28?p.getLongVersionCode():p.versionCode;
            return p.versionName+" ("+code+")";
        } catch(Exception ignored){ return "1.0 (1)"; }
    }
    private LinearLayout.LayoutParams matchWrap(int bottom){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=bottom;return p;}
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
}
