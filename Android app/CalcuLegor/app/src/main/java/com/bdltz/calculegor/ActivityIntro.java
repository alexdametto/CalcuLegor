package com.bdltz.calculegor;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class ActivityIntro extends AppIntro {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        TextView txtDone = findViewById(R.id.done);
        txtDone.setTextColor(getResources().getColor(R.color.colorAccent));
        TextView txtSkip = findViewById(R.id.skip);
        txtSkip.setTextColor(getResources().getColor(R.color.colorAccent));

        addSlide(AppIntroFragment.newInstance(getString(R.string.key_benvenuto), "sans-serif", "", "sans-serif", R.drawable.icona, getResources().getColor(R.color.white), getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.white)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.key_titolo_seconda_slide), "sans-serif", getString(R.string.key_descrizione_seconda_slide), "sans-serif", R.drawable.calculation, getResources().getColor(R.color.white), getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.key_titolo_terza_slide), "sans-serif", getString(R.string.key_descrizione_terza_slide), "sans-serif", R.drawable.key, getResources().getColor(R.color.white), getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorPrimary)));

        askForPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);

        setIndicatorColor(getResources().getColor(R.color.colorAccent), R.color.colorPrimary);
        setImageNextButton(getResources().getDrawable(R.drawable.next));
        showSkipButton(false);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}
