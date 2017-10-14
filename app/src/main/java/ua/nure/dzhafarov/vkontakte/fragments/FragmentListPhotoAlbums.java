package ua.nure.dzhafarov.vkontakte.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ua.nure.dzhafarov.vkontakte.R;
import ua.nure.dzhafarov.vkontakte.activities.SingleFragmentActivity;
import ua.nure.dzhafarov.vkontakte.adapters.PhotoAlbumAdapter;
import ua.nure.dzhafarov.vkontakte.models.PhotoAlbum;
import ua.nure.dzhafarov.vkontakte.models.User;
import ua.nure.dzhafarov.vkontakte.utils.OnUserClickListener;
import ua.nure.dzhafarov.vkontakte.utils.OperationListener;
import ua.nure.dzhafarov.vkontakte.utils.VKManager;

import static ua.nure.dzhafarov.vkontakte.activities.ActivityUserProfile.REQUEST_USER_PROFILE;

public class FragmentListPhotoAlbums extends Fragment {

    private List<PhotoAlbum> photoAlbums;
    private PhotoAlbumAdapter photoAlbumAdapter;
    private VKManager vkManager;
    private User owner;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    public static FragmentListPhotoAlbums newInstance(User owner) {
        Bundle args = new Bundle();
        args.putSerializable(REQUEST_USER_PROFILE, owner);
        FragmentListPhotoAlbums result = new FragmentListPhotoAlbums();
        result.setArguments(args);
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        owner = (User) getArguments().getSerializable(REQUEST_USER_PROFILE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_photos_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        photoAlbums = new ArrayList<>();
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_photos);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPhotoAlbums();
            }
        });
        swipeRefreshLayout.setColorSchemeColors(getActivity().getColor(R.color.colorPrimary));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        photoAlbumAdapter = new PhotoAlbumAdapter(photoAlbums, getActivity(), new OnUserClickListener<PhotoAlbum>() {
            @Override
            public void onUserClicked(PhotoAlbum result, View view) {
                startLoadingPhotos(result);
            }
        });

        recyclerView.setAdapter(photoAlbumAdapter);
        vkManager = VKManager.getInstance();


        getActivity().setTitle(String.format(Locale.ROOT, "%s's photo albums", owner.getFirstName()));
        loadPhotoAlbums();
    }

    private void loadPhotoAlbums() {
        swipeRefreshLayout.setRefreshing(true);
        vkManager.loadPhotoAlbums(owner.getId(), new OperationListener<List<PhotoAlbum>>() {
            @Override
            public void onSuccess(List<PhotoAlbum> object) {
                loadPhotosAlbumsInUI(object);
            }

            @Override
            public void onFailure(String message) {
                showErrorInUI(message);
            }
        });
    }

    private void loadPhotosAlbumsInUI(final List<PhotoAlbum> phs) {
        Activity activity = getActivity();

        if (activity != null) {
            activity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            photoAlbums.clear();
                            photoAlbums.addAll(phs);
                            photoAlbumAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
            );
        }
    }

    private void showErrorInUI(final String message) {
        final Activity activity = getActivity();

        if (activity != null) {
            activity.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }
    }

    private void startLoadingPhotos(PhotoAlbum album) {
        FragmentListPhotos listPhotos = FragmentListPhotos.newInstance(owner, album);
        SingleFragmentActivity.addFragmentToActivity(listPhotos, getActivity(), R.id.fragment_host);
    }
}
