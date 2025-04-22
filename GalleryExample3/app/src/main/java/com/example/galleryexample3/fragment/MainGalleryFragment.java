package com.example.galleryexample3.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.galleryexample3.MainActivityNew;
import com.example.galleryexample3.R;
import com.example.galleryexample3.SingleImageView;
import com.example.galleryexample3.businessclasses.ImageGalleryProcessing;
import com.example.galleryexample3.dataclasses.DatabaseHandler;
import com.example.galleryexample3.userinterface.GalleryImageGridAdapter;
import com.example.galleryexample3.userinterface.ItemClickSupporter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashSet;

public class MainGalleryFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {
    RecyclerView gridRecyclerView;
    LinearLayout optionBars;
    private ArrayList<String> imagesList;
    boolean selectionEnabled = false;
    DatabaseHandler databaseHandler;

    public MainGalleryFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Set up data
        View view = inflater.inflate(R.layout.main_gallery_fragment, container, false);
        imagesList = ImageGalleryProcessing.getImages(requireContext(), "DATE_ADDED", " ASC");
        databaseHandler = DatabaseHandler.getInstance(requireContext());

        // RecyclerView
        gridRecyclerView = view.findViewById(R.id.gridRecyclerView);
        GalleryImageGridAdapter galleryAdapter = new GalleryImageGridAdapter(requireContext(), imagesList);
        gridRecyclerView.setAdapter(galleryAdapter);
        gridRecyclerView.scrollToPosition(imagesList.size() - 1);

        Toolbar myToolbar = (Toolbar) requireActivity().findViewById(R.id.toolBar);
        myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int selectedId = item.getItemId();
                if (selectedId == R.id.filterButton) {
                    PopupMenu popup = new PopupMenu(requireContext(), myToolbar, Gravity.END);
                    popup.inflate(R.menu.filter_menu_main);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            int id = menuItem.getItemId();
                            if (id == R.id.name_asc){
                                imagesList = ImageGalleryProcessing.getImages(requireContext(), "DISPLAY_NAME", " ASC");
                            }
                            if (id == R.id.name_desc){
                                imagesList = ImageGalleryProcessing.getImages(requireContext(), "DISPLAY_NAME", " DESC");

                            }
                            if (id == R.id.date_asc){
                                imagesList = ImageGalleryProcessing.getImages(requireContext(), "DATE_ADDED", " ASC");

                            }
                            if (id == R.id.date_desc){
                                imagesList = ImageGalleryProcessing.getImages(requireContext(), "DATE_ADDED", " DESC");

                            }
                            if (id == R.id.size_asc){
                                imagesList = ImageGalleryProcessing.getImages(requireContext(), "SIZE", " ASC");

                            }
                            if (id == R.id.size_desc){
                                imagesList = ImageGalleryProcessing.getImages(requireContext(), "SIZE", " DESC");

                            }
                            final GalleryImageGridAdapter tempAdapter = new GalleryImageGridAdapter(requireContext(), imagesList);
                            gridRecyclerView.setAdapter(tempAdapter);
                            gridRecyclerView.scrollToPosition(imagesList.size() - 1);
                            return true;
                        }
                    });

                    popup.show();
                }
                    return true;
            }
        });


//        myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                int selectedId = item.getItemId();
//                if (selectedId == R.id.filterButton){
//                    View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.spinner_dialog_layout, null);
//                    Spinner spinner = dialogView.findViewById(R.id.spinnerDialog);
//                    final int[] selectedOption = {0};
//                    String[] options = new String[]{
//                            "Name Ascending",
//                            "Name Descending",
//                            "Date Ascending",
//                            "Date Descending",
//                            "Size Ascending",
//                            "Size Descending"
//                    };
//                    spinner.setAdapter(new ArrayAdapter<String>(requireContext(), R.layout.spinner_element, options));
//                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                        @Override
//                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                            selectedOption[0] = i;
//                        }
//
//                        @Override
//                        public void onNothingSelected(AdapterView<?> adapterView) {
//
//                        }
//                    });
//
//                    AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
//                            .setTitle("Set Layout Filter")
//                            .setView(dialogView)
//                            .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    switch (selectedOption[0]){
//                                        case 0:
//                                            imagesList = ImageGalleryProcessing.getImages(requireContext(), "DISPLAY_NAME", " ASC");
//                                            break;
//                                        case 1:
//                                            imagesList = ImageGalleryProcessing.getImages(requireContext(), "DISPLAY_NAME", " DESC");
//                                            break;
//                                        case 2:
//                                            imagesList = ImageGalleryProcessing.getImages(requireContext(), "DATE_ADDED", " ASC");
//                                            break;
//                                        case 3:
//                                            imagesList = ImageGalleryProcessing.getImages(requireContext(), "DATE_ADDED", " DESC");
//                                            break;
//                                        case 4:
//                                            imagesList = ImageGalleryProcessing.getImages(requireContext(), "SIZE", " ASC");
//                                            break;
//                                        case 5:
//                                            imagesList = ImageGalleryProcessing.getImages(requireContext(), "SIZE", " DESC");
//                                            break;
//                                        default:
//                                            imagesList = ImageGalleryProcessing.getImages(requireContext(), "DATE_ADDED", " ASC");
//                                            break;
//
//                                    }
//
//                                    final GalleryImageGridAdapter tempAdapter = new GalleryImageGridAdapter(requireContext(), imagesList);
//                                    gridRecyclerView.setAdapter(tempAdapter);
//                                    gridRecyclerView.scrollToPosition(imagesList.size() - 1);
//                                    dialogInterface.dismiss();
//                                }
//                            })
//                            .setNegativeButton("Close", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    dialogInterface.dismiss();
//                                }
//                            }).create();
//                    alertDialog.show();
//
//                    return true;
//                }
//
//                return false;
//            }
//        });

        // OptionBars
        optionBars = (LinearLayout) view.findViewById(R.id.optionBars);
        ImageButton deleteButton = (ImageButton) view.findViewById(R.id.deleteButton);
        ImageButton moreOptionButton = (ImageButton) view.findViewById(R.id.moreOptionButton);
        Button cancelSelectionButton = (Button) view.findViewById(R.id.cancelSelectionButton);
        TextView selectionTextView = (TextView) view.findViewById(R.id.selectionTextView);

        // Set up on click events
        moreOptionButton.setOnClickListener(this::showMenu);

        cancelSelectionButton.setOnClickListener(v -> {
            selectionEnabled = false;
            galleryAdapter.setSelectionMode(selectionEnabled);

            optionBars.setVisibility(View.GONE);
            if (getActivity() instanceof MainActivityNew) {
                ((MainActivityNew) getActivity()).showBottomNavigation();
            }
        });

        // Select image or enter details view
        ItemClickSupporter.addTo(gridRecyclerView).setOnItemClickListener(new ItemClickSupporter.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                Context context = requireContext();
                if (selectionEnabled) {
                    galleryAdapter.toggleSelection(position);
                    int selectedImagesCount = galleryAdapter.getSelectedImagesCount();
                    if (selectedImagesCount != 0)
                        selectionTextView.setText("Selected " + selectedImagesCount + " image" + (selectedImagesCount > 1 ? "s" : ""));
                    else
                        selectionTextView.setText("Select image");
                } else {
                    Log.e("Position", String.valueOf(position));
                    String imageUri = imagesList.get(position);
                    String dateAdded = ImageGalleryProcessing.getImageDateAdded(context, imageUri);
                    Intent intent = new Intent(context, SingleImageView.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("imageURI", imageUri);
                    bundle.putString("dateAdded", dateAdded);
                    bundle.putInt("position", position);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });

        // Enter multiple selection mode
        ItemClickSupporter.addTo(gridRecyclerView).setOnItemLongClickListener(new ItemClickSupporter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
                if (!selectionEnabled) {
                    selectionEnabled = true;
                    galleryAdapter.setSelectionMode(selectionEnabled);
                    galleryAdapter.toggleSelection(position);
                    selectionTextView.setText("Selected 1 image");

                    optionBars.setVisibility(View.VISIBLE);
                    if (getActivity() instanceof MainActivityNew) {
                        BottomNavigationView bottomNavView = (BottomNavigationView) getActivity().findViewById(R.id.bottomNavigationBar);
                        optionBars.setMinimumHeight(bottomNavView.getHeight());
                        optionBars.setPadding(0, 0, 0, bottomNavView.getHeight() / 2);
                        ((MainActivityNew) getActivity()).hideBottomNavigation();
                    }

                    gridRecyclerView.scrollToPosition(position);
                }
                return true;
            }
        });

        return view;
    }

    private void showMenu(View view) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.main_menu);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        Context context = requireContext();
        int id = menuItem.getItemId();
        GalleryImageGridAdapter galleryAdapter = (GalleryImageGridAdapter) gridRecyclerView.getAdapter();
        if (id == R.id.addAlbum) {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.one_field_dialog_layout, null);
            TextInputLayout inputTextLayout = dialogView.findViewById(R.id.inputTextLayout);
            TextInputEditText editText = dialogView.findViewById(R.id.editText);
            inputTextLayout.setHint("Enter Album Name");
            AlertDialog alertDialog = new AlertDialog.Builder(context)
                    .setTitle("Add Album")
                    .setView(dialogView)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String albumName = editText.getText().toString();
                            HashSet<Integer> selectedPositions = galleryAdapter.getSelectedPositions();

                            for (int position : selectedPositions)
                                databaseHandler.albums().addImageToAlbum(albumName, imagesList.get(position));

                            selectionEnabled = false;
                            galleryAdapter.setSelectionMode(selectionEnabled);
                            optionBars.setVisibility(View.GONE);
                            if (getActivity() instanceof MainActivityNew) {
                                ((MainActivityNew) getActivity()).showBottomNavigation();
                            }

                            Toast.makeText(context, "Added to " + albumName, Toast.LENGTH_LONG).show();
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            dialogInterface.dismiss();
                        }
                    }).create();
            alertDialog.show();
            return true;
        }
        else if (id == R.id.addTag) {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.one_field_dialog_layout, null);
            TextInputLayout inputTextLayout = dialogView.findViewById(R.id.inputTextLayout);
            TextInputEditText editText = dialogView.findViewById(R.id.editText);
            inputTextLayout.setHint("Enter Tag Name");
            AlertDialog alertDialog = new AlertDialog.Builder(context)
                    .setTitle("Add Tag")
                    .setView(dialogView)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String tagName = editText.getText().toString();

                            Toast.makeText(context, "Added to " + tagName, Toast.LENGTH_LONG).show();
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            dialogInterface.dismiss();
                        }
                    }).create();
            alertDialog.show();
            return true;
        } else
            return false;
    }
}