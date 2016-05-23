package it.piemonte.arpa.openoise;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

public class CreditsActivity extends AppCompatActivity {

    private TextView created;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credits);

        created = (TextView) findViewById(R.id.created);
//        if (contacts != null) {
//            contacts.setMovementMethod(LinkMovementMethod.getInstance());
//        }
        Linkify.addLinks(created, Linkify.WEB_URLS);
    }
}
