package com.akhris.pregnytalk.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.RadioButton;

import com.akhris.pregnytalk.R;
import com.akhris.pregnytalk.contract.Child;
import com.akhris.pregnytalk.utils.DateUtils;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;

/**
 * Dialog fragment representing adding child form.
 */
public class AddChildFragment extends DialogFragment {

    @BindView(R.id.et_add_child_name) EditText mName;
    @BindView(R.id.et_add_child_date_of_birth) EditText mDateOfBirthEditText;
    @BindView(R.id.rb_sex_male) RadioButton mMaleRadioButton;
    @BindView(R.id.rb_sex_female) RadioButton mFemaleRadioButton;


    private long mDateOfBirth;

    //Parent of that fragment must implement this Callback to get a Child instance back
    private Callback mCallback;


    public AddChildFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof Callback){
            mCallback = (Callback)context;
        }
    }

    /**
     * Making Dialog without additional space for title (because we have the custom one).
     * Solution got here:
     * https://stackoverflow.com/a/15279400/7635275
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if(dialog.getWindow()==null){return dialog;}
        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getParentFragment() instanceof Callback){
            mCallback = (Callback) getParentFragment();
        }

        if(mCallback==null){
            String contextString = getContext()==null?"":(getContext().toString()+" ");
            String parentFragmentString = getParentFragment()==null?"":(getParentFragment().toString()+" ");
            throw new UnsupportedOperationException(
                    String.format("Either context %sor parent fragment %sshould implement Callback",
                            contextString, parentFragmentString)
            );
        }

        this.mDateOfBirth = System.currentTimeMillis();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_add_child, container, false);
        ButterKnife.bind(this, mRootView);
        return mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Random rnd = new Random();

        if(rnd.nextBoolean()){
            mFemaleRadioButton.setChecked(true);
        } else {
            mMaleRadioButton.setChecked(true);
        }
        setDateOfBirthText();
    }

    /**
     * Using DatePicker to pick a birth date.
     * It is shown when corresponding edittext object is focused
     * (to prevent incorrect date input)
     */
    @OnFocusChange(R.id.et_add_child_date_of_birth)
    public void onEditDateOfBirthClick(boolean isFocused){
        if(isFocused) {
            DateUtils.showDatePicker(
                    getContext(),
                    mDateOfBirth,
                    timeInMillis -> {
                        mDateOfBirth = timeInMillis;
                        setDateOfBirthText();
                    }
            );
        }
    }

    private void setDateOfBirthText() {
        this.mDateOfBirthEditText.setText(
                DateUtils.formatDateFromMillis(mDateOfBirth)
        );
    }

    /**
     * After clicking on Add Child button - make instance of Child class and return it via callback
     */
    @OnClick(R.id.b_add_child)
    public void onAddChildClick(){
        Child child = new Child();
        child.setName(mName.getText().toString());
        child.setSex(
                mFemaleRadioButton.isChecked()?Child.SEX_FEMALE:Child.SEX_MALE
        );
        child.setBirthDateMillis(mDateOfBirth);
        mCallback.onChildAdded(child);
        dismiss();
    }

    public interface Callback{
        void onChildAdded(Child child);
    }
}
