package com.alexhogberg.android.bikeparkmaterial;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * Created by ahogberg on 2015-09-14.
 */
public class CustomDialogBuilder extends Dialog {

    /*
    * @TODO: Implement funcitonality for replacing the old method of creating dialogs
    */
    private String name;
    public static EditText etName;
    public String zip;
    OnMyDialogResult mDialogResult; // the callback

    public CustomDialogBuilder(Context context, String name) {
        super(context);
        this.name = name;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // same you have
    }

    private class OKListener implements android.view.View.OnClickListener {
        @Override
        public void onClick(View v) {
            if( mDialogResult != null ){
                mDialogResult.finish(String.valueOf(etName.getText()));
            }
            CustomDialogBuilder.this.dismiss();
        }
    }

    public void setDialogResult(OnMyDialogResult dialogResult){
        mDialogResult = dialogResult;
    }

    public interface OnMyDialogResult{
        void finish(String result);
    }
}
