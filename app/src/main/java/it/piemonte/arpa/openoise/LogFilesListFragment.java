package it.piemonte.arpa.openoise;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;



import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LogFilesListFragment extends ListFragment implements OnItemClickListener {

    private String path;

    public final static String LOG_FILE_NAME = "it.piemonte.arpa.LogText";
    public final static String LOG_FILE_TEXT = "it.piemonte.arpa.LogText";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_files_list, container, false);



        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        path = Environment.getExternalStorageDirectory() + File.separator + "openoise";

        // Read all files sorted into the values-array
        List values = new ArrayList();
        File dir = new File(path);
        if (!dir.canRead()) {
            Toast.makeText(getActivity(), "Cannot read directory" + "\n" + "Maybe you have never recorded log files", Toast.LENGTH_SHORT).show();
        }

        String[] list = dir.list();

        if (list != null) {
            for (String file : list) {
                if (!file.startsWith(".")) {
                    values.add(file);
                }
            }
        }
        Collections.sort(values);

        ArrayAdapter adapter = new ArrayAdapter(getActivity(),android.R.layout.simple_list_item_1,android.R.id.text1,values);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
        Toast.makeText(getActivity(), "Loading file", Toast.LENGTH_SHORT).show();

        String filename = (String) getListAdapter().getItem(position);

        Intent intent = new Intent(getActivity(), LogFilesReadActivity.class);
        intent.putExtra("LOG_FILE_NAME", filename);

        startActivity(intent);

    }
}
