package com.ashomok.imagetotext.language_choser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.ashomok.imagetotext.R;
import com.ashomok.imagetotext.Settings;
import com.ashomok.imagetotext.utils.LogUtil;
import com.ashomok.imagetotext.utils.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by iuliia on 10/22/17.
 */

//MINOR todo add search view https://developer.android.com/training/search/search.html (add add async loader firstly because of technical reasons)
//MINOR todo add async loader for fill recyclerviews LoaderManager.LoaderCallbacks<List<String>>
//todo may be use rx for manipulation with two adapters
public class LanguageOcrActivity extends AppCompatActivity {
    private static final String TAG = LogUtil.DEV_TAG + LanguageOcrActivity.class.getSimpleName();
    public static final String CHECKED_LANGUAGE_CODES = "checked_languages_set";
    private List<String> recentlyChosenLanguageCodes;
    private boolean isAuto;
    private LanguagesListAdapter.ResponsableList<String> checkedLanguageCodes;
    private LanguagesListAdapter allLangAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_ocr);

        initToolbar();

        @Nullable List<String> list = obtainCheckedLanguageCodes();
        checkedLanguageCodes = (list == null) ?
                new LanguagesListAdapter.ResponsableList<>(new ArrayList<>())
                : new LanguagesListAdapter.ResponsableList<>(list);

        StateChangedNotifier notifier = isAutoChecked -> {
            if (!isAutoChecked) {
                isAuto = false;
                updateAutoUi(isAuto);
            }
        };

        //init recently chosen language list
        recentlyChosenLanguageCodes = obtainRecentlyChosenLanguageCodes();
        LanguagesListAdapter recentlyChosenLangAdapter = null;
        if (recentlyChosenLanguageCodes.size() > 0) {
            View recentlyChosen = findViewById(R.id.recently_chosen);
            recentlyChosen.setVisibility(View.VISIBLE);
            RecyclerView recyclerViewRecentlyChosen = findViewById(R.id.recently_chosen_list);
            recyclerViewRecentlyChosen.setHasFixedSize(true);
            LinearLayoutManager recentlyChosenLayoutManager = new LinearLayoutManager(this);
            recyclerViewRecentlyChosen.setLayoutManager(recentlyChosenLayoutManager);

            recentlyChosenLangAdapter = new LanguagesListAdapter(this,
                    recentlyChosenLanguageCodes, checkedLanguageCodes, notifier);
            recyclerViewRecentlyChosen.setAdapter(recentlyChosenLangAdapter);
        }

        //init all languages list
        RecyclerView recyclerViewAllLanguages = findViewById(R.id.all_languages_list);
        recyclerViewAllLanguages.setHasFixedSize(true);
        LinearLayoutManager allLanguagesLayoutManager = new LinearLayoutManager(this);
        recyclerViewAllLanguages.setLayoutManager(allLanguagesLayoutManager);
        List<String> allLanguageCodes = obtainAllLanguageCodes();
        allLangAdapter = new LanguagesListAdapter(this,
                allLanguageCodes, checkedLanguageCodes, notifier);
        recyclerViewAllLanguages.setAdapter(allLangAdapter);

        //init auto btn
        if (checkedLanguageCodes.size() < 1) {
            //check auto btn
            isAuto = true;
        }
        LinearLayout autoBtn = findViewById(R.id.auto);
        updateAutoUi(isAuto);

        LanguagesListAdapter finalRecentlyChosenLangAdapter = recentlyChosenLangAdapter;
        autoBtn.setOnClickListener(view -> {
            isAuto = !isAuto;
            updateAutoUi(isAuto);

            if (finalRecentlyChosenLangAdapter != null) {
                finalRecentlyChosenLangAdapter.onAutoStateChanged(isAuto);
            }
            allLangAdapter.onAutoStateChanged(isAuto);
        });
    }


    private List<String> obtainAllLanguageCodes() {
        return new ArrayList<>(Settings.getOcrLanguageSupportList(this).keySet());
    }

    /**
     * obtain recently chosen Languages from SharedPreferences in order: first - the most recently chosen.
     * Max 5 recently chosen Languages allowed.
     *
     * @return recently chosen Languages
     */
    private
    List<String> obtainRecentlyChosenLanguageCodes() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        List<String> recentlyChosenLanguageCodes = SharedPreferencesUtil.pullStringList(
                sharedPref, getString(R.string.recently_chosen_languge_codes));
        if (recentlyChosenLanguageCodes == null) {
            recentlyChosenLanguageCodes = new ArrayList<>();
        }
        return recentlyChosenLanguageCodes;
    }

    /**
     * if returns null - auto detection is checked
     *
     * @return checked language keys or null, which means auto detection is checked
     */
    private @Nullable
    List<String> obtainCheckedLanguageCodes() {
        Intent intent = getIntent();
        ArrayList<String> extra = intent.getStringArrayListExtra(CHECKED_LANGUAGE_CODES);
        if (extra != null) {
            return extra;
        } else {
            return null;
        }
    }

    /**
     * call before finish activity
     */
    private void saveRecentlyChosenLanguages() {
        LinkedHashSet<String> languagesSet = new LinkedHashSet<>();
        languagesSet.addAll(checkedLanguageCodes);
        languagesSet.addAll(recentlyChosenLanguageCodes);

        List<String> languagesSubList =
                Stream.of(languagesSet).limit(5).collect(Collectors.toList());

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferencesUtil.pushStringList(sharedPref,
                languagesSubList, getString(R.string.recently_chosen_languge_codes));
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> {
            //back btn pressed
            Intent intent = new Intent();
            saveRecentlyChosenLanguages();
            if (checkedLanguageCodes != null) {
                intent.putExtra(CHECKED_LANGUAGE_CODES, checkedLanguageCodes);
                setResult(RESULT_OK, intent);
            }

            finish();
        });
    }

    void updateAutoUi(boolean checked) {
        View checkedIcon = findViewById(R.id.checked_icon);
        checkedIcon.setVisibility(checked ? View.VISIBLE : View.GONE);
        View autoIcon = findViewById(R.id.auto_icon);
        autoIcon.setVisibility(checked ? View.GONE : View.VISIBLE);
    }

    public interface StateChangedNotifier {
        void changeAutoState(boolean isAutoChecked);
    }
}
