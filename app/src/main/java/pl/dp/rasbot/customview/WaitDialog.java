package pl.dp.rasbot.customview;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pl.dp.rasbot.R;

/**
 * Created by Project4You S.C. on 26.10.15.
 * Author: Dawid Podolak
 * Email: dawidpod1@gmail.com
 * All rights reserved!
 */
public class WaitDialog extends Dialog {

    @InjectView(R.id.tvWDTitle)
    TextView titleTextView;

    @InjectView(R.id.tvWDContent)
    TextView contentTextView;

    private String title, content;

    public WaitDialog(Context context, String title, String content) {
        super(context);
        this.title = title;
        this.content = content;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.wait_dialog);
        ButterKnife.inject(this);

        titleTextView.setText(title);
        contentTextView.setText(content);
    }

    public void setContent(String content){
        contentTextView.setText(content);
    }

    public void setContent(int stringRes){
        contentTextView.setText(getContext().getString(stringRes));
    }
}
