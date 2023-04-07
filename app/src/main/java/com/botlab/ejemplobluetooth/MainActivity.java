package com.botlab.ejemplobluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.slider.Slider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    EditText edtTextoOut;
    ImageButton btnEnviar, btnAdelante, btnIzquierda, btnStop, btnDerecha, btnReversa;
    TextView tvtMensaje;
    Button btnDesconectar;
    Slider mySlider;
    int count = 0;

    //-------------------------------------------
    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder DataStringIN = new StringBuilder();
    private ConnectedThread MyConexionBT;
    // Identificador unico de servicio - SPP UUID
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String para la direccion MAC
    private static String address = null;
    //-------------------------------------------

    @SuppressLint({"ClickableViewAccessibility", "HandlerLeak"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothIn = new Handler() {
                public void handleMessage(android.os.Message msg) {

                if (msg.what == handlerState) {

                    //Interacción con los datos de ingreso
                    //char MyCaracter = (char) msg.obj;
                    String myString = (String) msg.obj;
                    //DataStringIN.append(myString);
                    //tvtMensaje.setText(myString);
                    DataStringIN.append(myString);
                    int endOfLineIndex = DataStringIN.indexOf("#");

                    if (endOfLineIndex > 0) {
                        String dataInPrint = DataStringIN.substring(0, endOfLineIndex);
                        tvtMensaje.setText(dataInPrint);
                        DataStringIN.delete(0, DataStringIN.length());
                    }

                    /*if(MyCaracter == "a"){
                        count++;
                        String c = String.valueOf(count);
                        tvtMensaje.setText(c);
                    }

                    if(MyCaracter == "i"){
                        tvtMensaje.setText("GIRO IZQUIERDA");
                    }

                    if(MyCaracter == "d"){
                        tvtMensaje.setText("GIRO DERECHA");
                    }

                    if(MyCaracter == "r"){
                        tvtMensaje.setText("RETROCEDIENDO");
                    }

                    if(MyCaracter == "s"){
                        tvtMensaje.setText("DETENIDO");
                    }*/

                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth adapter
        VerificarEstadoBT();

        edtTextoOut = findViewById(R.id.edtTextoOut);
        btnEnviar = findViewById(R.id.btnEnviar);
        btnAdelante = findViewById(R.id.btnAdelante);
        btnIzquierda = findViewById(R.id.btnIzquierda);
        btnStop = findViewById(R.id.btnStop);
        btnDerecha = findViewById(R.id.btnDerecha);
        btnReversa = findViewById(R.id.btnReversa);
        tvtMensaje = findViewById(R.id.tvtMensaje);
        btnDesconectar = findViewById(R.id.btnDesconectar);

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String GetDat = edtTextoOut.getText().toString();
                //tvtMensaje.setText(GetDat);
                MyConexionBT.write(GetDat);
            }
        });

        btnAdelante.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        MyConexionBT.write("U");
                        break;
                    case MotionEvent.ACTION_UP:
                        MyConexionBT.write("S");
                        break;
                }
                return false;
            }
        });

        btnIzquierda.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        MyConexionBT.write("L");
                        break;
                    case MotionEvent.ACTION_UP:
                        MyConexionBT.write("S");
                        break;
                }
                return false;
            }
        });


        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyConexionBT.write("X");
            }
        });

        btnDerecha.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        MyConexionBT.write("R");
                        break;
                    case MotionEvent.ACTION_UP:
                        MyConexionBT.write("S");
                        break;
                }
                return false;
            }
        });

        btnReversa.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        MyConexionBT.write("D");
                        break;
                    case MotionEvent.ACTION_UP:
                        MyConexionBT.write("S");
                        break;
                }
                return false;
            }
        });

        btnDesconectar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btSocket != null) {
                    try {
                        btSocket.close();
                    } catch (IOException e) {
                        Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_SHORT).show();
                        ;
                    }
                }
                finish();
            }
        });
    }

        private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
        {
            //crea un conexion de salida segura para el dispositivo usando el servicio UUID
            return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        }

        @Override
        public void onResume()
        {
            super.onResume();

            Intent intent = getIntent();
            address = intent.getStringExtra(DispositivosVinculados.EXTRA_DEVICE_ADDRESS);
            //Setea la direccion MAC
            BluetoothDevice device = btAdapter.getRemoteDevice(address);

            try
            {
                btSocket = createBluetoothSocket(device);
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
            }
            // Establece la conexión con el socket Bluetooth.
            try
            {
                btSocket.connect();
            } catch (IOException e) {
                try {
                    btSocket.close();
                } catch (IOException e2) {}
            }
            MyConexionBT = new ConnectedThread(btSocket);
            MyConexionBT.start();
        }

        @Override
        public void onPause() {
            super.onPause();
            try
            { // Cuando se sale de la aplicación esta parte permite que no se deje abierto el socket
                btSocket.close();
            } catch (IOException e2) {}
        }

        //Comprueba que el dispositivo Bluetooth
        //está disponible y solicita que se active si está desactivado

        private void VerificarEstadoBT() {

            if(btAdapter==null) {
                Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
            } else {
                if (btAdapter.isEnabled()) {
                } else {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);
                }
            }
        }

        //Crea la clase que permite crear el evento de conexion
        private class ConnectedThread extends Thread
        {
            private final InputStream mmInStream;
            private final OutputStream mmOutStream;

            public ConnectedThread(BluetoothSocket socket)
            {
                InputStream tmpIn = null;
                OutputStream tmpOut = null;
                try
                {
                    tmpIn = socket.getInputStream();
                    tmpOut = socket.getOutputStream();
                } catch (IOException e) { }
                mmInStream = tmpIn;
                mmOutStream = tmpOut;
            }

            public void run() {
                byte[] buffer = new byte[256];
                int bytes;

                // Keep looping to listen for received messages
                while (true) {
                    try {
                        bytes = mmInStream.read(buffer);         //read bytes from input buffer
                        String readMessage = new String(buffer, 0, bytes);
                        // Send the obtained bytes to the UI Activity via handler
                        bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                    } catch (IOException e) {
                        break;
                    }
                }
            }

            /*public void run()
            {
                byte[] byte_in = new byte[1];
                // Se mantiene en modo escucha para determinar el ingreso de datos
                while (true) {
                    try {
                        mmInStream.read(byte_in);
                        char ch = (char) byte_in[0];
                        bluetoothIn.obtainMessage(handlerState, ch).sendToTarget();
                    } catch (IOException e) {
                        break;
                    }
                }
            }*/

            //Envio de trama
            public void write(String input)
            {
                try {
                    mmOutStream.write(input.getBytes());
                }
                catch (IOException e)
                {
                    //si no es posible enviar datos se cierra la conexión
                    Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
}