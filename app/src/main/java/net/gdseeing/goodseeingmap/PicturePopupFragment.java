package net.gdseeing.goodseeingmap;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import net.gdseeing.goodseeingmap.backend_connection.PictureData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PicturePopupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PicturePopupFragment extends DialogFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String tag = "picture_data";

    // TODO: Rename and change types of parameters
    private PictureData pictureData;


    public PicturePopupFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static PicturePopupFragment newInstance(PictureData pictureData) throws JsonProcessingException {
        PicturePopupFragment fragment = new PicturePopupFragment();
        Bundle args = new Bundle();
        args.putString(tag, pictureData.json());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ObjectMapper objectMapper = new ObjectMapper();
        if (getArguments() != null) {
            try {
                pictureData = objectMapper.readValue(getArguments().getString(tag), PictureData.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflated =  inflater.inflate(R.layout.fragment_picture_popup, container, false);
        // Inflate the layout for this fragment
        ImageView imageView = (ImageView) inflated.findViewById(R.id.fragment_image);
        LinearLayout linerLayout = (LinearLayout)inflated.findViewById(R.id.dialog_layout);
        TextView textView = (TextView)inflated.findViewById(R.id.fragment_text) ;
        File file = new File( getContext().getCacheDir(), pictureData.getPict_id());
        linerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        textView.setText(pictureData.getTitle());

        try {
            imageView.setImageBitmap(getBitmapFromFile(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return inflated;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){

    }
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        Dialog dialog = new Dialog(requireContext());
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels * 0.7),(int)(getResources().getDisplayMetrics().heightPixels * 0.2));

        return dialog;

    }
    private Bitmap getBitmapFromFile(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);

        Bitmap image = BitmapFactory.decodeFileDescriptor(fileInputStream.getFD());

        fileInputStream.close();

        return image;
    }
}