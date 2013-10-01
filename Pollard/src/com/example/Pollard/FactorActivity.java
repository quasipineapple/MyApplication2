package com.example.Pollard;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.math.BigInteger;


public class FactorActivity extends Activity {

    private Button startButton, stopButton, clearButton, constButton;
    private TextView tvIn, tvOut;
    PollardRho pr = new PollardRho();

    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        startButton = (Button)this.findViewById(R.id.startClick);
        stopButton  = (Button)this.findViewById(R.id.stopClick);
        clearButton = (Button)this.findViewById(R.id.clearClick);
        constButton = (Button)this.findViewById(R.id.constClick);

        tvIn  = (TextView)this.findViewById(R.id.Eingabe);
        tvOut = (TextView)this.findViewById(R.id.Ausgabe);

        this.startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressStart();
            }
        });

        this.stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressStop();
            }
        });

        this.clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressClear();
            }
        });

        this.constButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pressConst();
            }
        });
    }

    private void pressStart()
    {
        long start = System.currentTimeMillis();
        pr.clear();
        pr.factor(new BigInteger(tvIn.getText().toString()));
        long millis = System.currentTimeMillis() - start;
        tvOut.append(pr.getfactors());
        Log.d("PollardRho", "Time used: " + String.valueOf(millis));
        tvOut.append("Time used: " + String.valueOf(millis) + "\n");
    }

    private void pressStop()
    {
        this.finish();
    }

    private void pressClear()
    {
        tvIn.setText("");
        tvOut.setText("");
    }

    private void pressConst()
    {
        BigInteger n = new BigInteger("13");
        n = n.multiply(new BigInteger("53"));
        n = n.multiply(new BigInteger("541"));
        n = n.multiply(new BigInteger("18059"));
        n = n.multiply(new BigInteger("771938043575157247596130369"));
        n = n.multiply(new BigInteger("43794578693413110408820225376161"));
        n = n.add(new BigInteger("241"));
        tvIn.setText(n.toString());
    }
}




