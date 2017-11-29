package com.ashomok.imagetotext.ocr_result.tab_fragments;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.ashomok.imagetotext.R;
import com.ashomok.imagetotext.ocr.OcrActivity;
import com.ashomok.imagetotext.ocr_result.translate.TranslateActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.CLIPBOARD_SERVICE;
import static com.ashomok.imagetotext.Settings.appPackageName;
import static com.ashomok.imagetotext.ocr.OcrActivity.RESULT_CANCELED_BY_USER;
import static com.ashomok.imagetotext.ocr_result.OcrResultActivity.LANGUAGE_CHANGED_REQUEST_CODE;
import static com.ashomok.imagetotext.utils.InfoSnackbarUtil.showWarning;
import static com.ashomok.imagetotext.utils.LogUtil.DEV_TAG;

/**
 * Created by iuliia on 5/31/17.
 */

//todo Forbidd splitter to go out of screen bounds. https://github.com/bieliaievays/OCRme/issues/2
public class TextFragment extends Fragment implements View.OnClickListener {
    public static final String EXTRA_TEXT = "com.ashomokdev.imagetotext.TEXT";
    public static final String EXTRA_IMAGE_URI = "com.ashomokdev.imagetotext.IMAGE";
    private CharSequence textResult;
    private Uri imageUri;
    private static final String TAG = DEV_TAG + TextFragment.class.getSimpleName();
    private static final int OCR_Activity_REQUEST_CODE = 2;
    private View mRootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.text_fragment, container, false);
        Bundle bundle = getArguments();
        textResult = bundle.getCharSequence(EXTRA_TEXT);
        imageUri = bundle.getParcelable(EXTRA_IMAGE_URI);

        mRootView = view;
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setImage();
        initText();
        initBottomPanel();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.copy_btn:
                copyTextToClipboard(textResult);
                break;
            case R.id.translate_btn:
                onTranslateClicked();
                break;
            case R.id.share_text_btn:
                onShareClicked();
                break;
            case R.id.bad_result_btn:
//                onBadResultClicked(); //todo
                break;
            default:
                break;
        }
    }

    private void initBottomPanel() {
        View copyBtn = getActivity().findViewById(R.id.copy_btn);
        copyBtn.setOnClickListener(this);

        View translateBtn = getActivity().findViewById(R.id.translate_btn);
        translateBtn.setOnClickListener(this);

        View shareBtn = getActivity().findViewById(R.id.share_text_btn);
        shareBtn.setOnClickListener(this);

        View badResult = getActivity().findViewById(R.id.bad_result_btn);
        badResult.setOnClickListener(this);
    }


    private void initText() {
        EditText mTextView = getActivity().findViewById(R.id.text);
        mTextView.setText(textResult);
    }

    private void setImage() {

        final ImageView mImageView = getActivity().findViewById(R.id.source_image);

        Glide.with(this)
                .load(imageUri)
                .signature(new StringSignature(String.valueOf(System.currentTimeMillis()))) //needs because image url not changed. It returns the same image all the time if remove this line. It is because default build-in cashe mechanism.
                .fitCenter()
                .crossFade()
                .into(mImageView);

        //scroll to centre
        final ScrollView scrollView = getActivity().findViewById(R.id.image_scroll_view);
        scrollView.addOnLayoutChangeListener((
                v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            int centreHeight = mImageView.getHeight() / 2;
            int centreWidth = mImageView.getWidth() / 2;
            scrollView.scrollTo(centreWidth, centreHeight);
        });
    }

//    //todo
//    private void onBadResultClicked() {
//        Intent intent = new Intent(getActivity(), LanguageActivity.class);
//        intent.putExtra(CHECKED_LANGUAGE_CODES, getCurrentLanguages());
//        startActivityForResult(intent, LANGUAGE_CHANGED_REQUEST_CODE);
//    }

//    //todo
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        if (requestCode == LANGUAGE_CHANGED_REQUEST_CODE && resultCode == RESULT_OK) {
//            //language was changed - run ocr again for the same image
//            Bundle bundle = data.getExtras();
//            ArrayList<String> updatedLanguages = bundle.getStringArrayList(CHECKED_LANGUAGE_CODES);
//            updateOcrResult(updatedLanguages);
//        }
//
//        //ocr canceled
//        else if (requestCode == OCR_Activity_REQUEST_CODE && resultCode == RESULT_CANCELED_BY_USER) {
//            showWarning(R.string.canceled, mRootView);
//        }
//    }
//
//    private void updateOcrResult(ArrayList<String> languages) {
//        String languagesList = Stream.of(languages).map(Object::toString).collect(Collectors.joining(", "));
//        Log.d(TAG, "Ocr called with new languages: " + languagesList);
//
//        startOcrActivity(imageUri, languages);
//
//    }

//    //todo
//    private void startOcrActivity(Uri uri, ArrayList<String> languages) {
//        Intent intent = new Intent(getActivity(), OcrActivity.class);
//        intent.setData(uri);
//        intent.putExtra(OcrActivity.EXTRA_LANGUAGES, obtainLanguageShortcuts(languages));
//        startActivityForResult(intent, OCR_Activity_REQUEST_CODE);
//    }
//
//    private ArrayList<String> obtainLanguageShortcuts(ArrayList<String> languages) {
//
//        LanguageList data = new LanguageList(getActivity());
//        LinkedHashMap<String, String> allLanguages = data.getLanguages();
//
//        ArrayList<String> result = new ArrayList<>();
//        for (String name : languages) {
//            if (allLanguages.containsKey(name)) {
//                result.add(allLanguages.get(name));
//            }
//        }
//
//        return result;
//    }

//    @NonNull
//    private ArrayList<String> getCurrentLanguages() {
//        Set<String> checkedLanguageNames = obtainSavedLanguages();
//        ArrayList<String> checkedLanguages = new ArrayList<>();
//        checkedLanguages.addAll(checkedLanguageNames);
//        return checkedLanguages;
//    }
//
//    private Set<String> obtainSavedLanguages() {
//
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        Set<String> auto = new HashSet<String>() {{
//            add(getString(R.string.auto));
//        }};
//        TreeSet<String> checkedLanguagesNames = new TreeSet<>(sharedPref.getStringSet(CHECKED_LANGUAGE_CODES, auto));
//        return checkedLanguagesNames;
//    }

    @SuppressWarnings("deprecation")
    private void onShareClicked() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        Resources res = getActivity().getResources();
        String linkToApp = "https://play.google.com/store/apps/details?id=" + appPackageName;
        String sharedBody =
                String.format(res.getString(R.string.share_text_message), textResult, linkToApp);

        Spanned styledText;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            styledText = Html.fromHtml(sharedBody, Html.FROM_HTML_MODE_LEGACY);
        } else {
            styledText = Html.fromHtml(sharedBody);
        }

        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, res.getString(R.string.text_result));
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, styledText);
        getActivity().startActivity(Intent.createChooser(sharingIntent, res.getString(R.string.send_text_result_to)));
    }

    private void onTranslateClicked() {
        Intent intent = new Intent(getActivity(), TranslateActivity.class);
        intent.putExtra(EXTRA_TEXT, textResult);
        startActivity(intent);
    }

    private void copyTextToClipboard(CharSequence text) {
        ClipboardManager clipboard =
                (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.text_result), text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getActivity(), getActivity().getString(R.string.copied),
                Toast.LENGTH_SHORT).show();
        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 300 milliseconds
        v.vibrate(300);
    }
}
