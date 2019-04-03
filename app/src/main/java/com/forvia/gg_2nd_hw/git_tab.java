package com.forvia.gg_2nd_hw;

import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;


public class git_tab extends Fragment {
    private ListView git_repo_list;
    private ArrayAdapter<String> arrayAdapter;
    private Context FragmentContext;
    final List<String> repo_lister = new ArrayList<String>();
    private FloatingActionButton fab;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.git_tab, container, false);
        /*šis nepieciešams lai nevajadzētu speciāli veidot atsevišķu CPU thread interneta savienojumam.*/
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        return view;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FragmentContext = getActivity().getApplicationContext();
        //*Saraksta attēlošanas ListView
        git_repo_list = (ListView) view.findViewById(R.id.repo_list);
        arrayAdapter = new ArrayAdapter<String>(FragmentContext, android.R.layout.simple_list_item_1, repo_lister);
        git_repo_list.setAdapter(arrayAdapter);

        //Refresh poga
        fab = (FloatingActionButton) getView().findViewById(R.id.fab2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                get_repo_list();
            }
        });
        //Izsaucam funkciju kas nolasa un saliek sarakstā repozitorijus no GIT.
        get_repo_list();
    }


    public void get_repo_list()
    {
        HttpURLConnection urlConnection;
        URL url;
        InputStream inputStream;

        /* nolasīsim tikai pēdejo 4h laikā izveidotos repozitorijus, citādi ļoti garš saraksts*/
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, -4);
        String date = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        String Time =   new SimpleDateFormat("HH:mm:ss").format(calendar.getTime());

        try{
            //git api saite + parametri
            url = new URL("https://api.github.com/search/repositories?q=is:public%20created:%3E"+date+"T"+Time+"Z");
            urlConnection = (HttpURLConnection) url.openConnection();

            /*autorizējamies (pa vienkāršo).*/
             String basicAuth = "Basic "+ Base64.encodeToString("forvia:makrele123".getBytes(), Base64.NO_WRAP);
             urlConnection.setRequestProperty ("Authorization", basicAuth);

            //izmantosim GET metodi.
            urlConnection.setRequestMethod("GET");

            urlConnection.setDoInput(true);//Nepieciešams lai vispār saņemtu atbildi.
            urlConnection.connect();

            //Apstrādājam atbildi.
            int httpStatus = urlConnection.getResponseCode();

            //if HTTP response is 200 i.e. HTTP_OK read inputstream else read errorstream
            /*paradam kļūdu, ja neizdodas pieslēgties*/
            if (httpStatus != HttpURLConnection.HTTP_OK) {

                inputStream = urlConnection.getErrorStream();
                Map<String, List<String>> map = urlConnection.getHeaderFields();
                for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                    Toast.makeText(FragmentContext,entry.getKey()  + " : " + entry.getValue(),Toast.LENGTH_LONG);
                                 }
            }

            /*Iegūstam datus*/
            else {
                inputStream = urlConnection.getInputStream();
            }

            //read inputstream
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String temp,response="";
            while((temp = bufferedReader.readLine())!=null){
                response+=temp;
            }


                JSONObject obj = (JSONObject) new JSONTokener(response).nextValue();
                JSONArray items = obj.getJSONArray("items");

                repo_lister.clear(); //Ja refrešo, tad izstīram esošo sarakstu
               //Ielasam mazāk par 100 ierakstiem (vairāk īsti nav nepieciešams).

                //ejam ciklā cauri saņemtajam JSON datiem.

            JSONObject obj1;
            String name;
            JSONObject owner;
            String owner_login;
            for (int x =0;x<items.length() && x < 100;x++)
                {
                     obj1 = items.getJSONObject(x);
                     name = obj1.getString("name");
                     owner = obj1.getJSONObject("owner");
                     owner_login = owner.getString("login");
                    repo_lister.add(name + " : " + owner_login);
                }
                arrayAdapter.notifyDataSetChanged();

            urlConnection.disconnect();
        } catch (MalformedURLException | ProtocolException | JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}




