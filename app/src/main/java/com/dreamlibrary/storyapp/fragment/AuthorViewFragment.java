package com.dreamlibrary.storyapp.fragment;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.dreamlibrary.storyapp.R;
import com.dreamlibrary.storyapp.activity.MainActivity;
import com.dreamlibrary.storyapp.item.ChildAuthorSection;
import com.dreamlibrary.storyapp.item.MainChildAuthorSection;
import com.dreamlibrary.storyapp.rest.ApiClient;
import com.dreamlibrary.storyapp.rest.ApiInterface;
import com.dreamlibrary.storyapp.util.API;
import com.dreamlibrary.storyapp.util.Method;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AuthorViewFragment extends Fragment {

    View view;
    RecyclerView recycler_author_view;
    private String type, title, id;
    private Method method;
    private LayoutAnimationController animation;
    private List<ChildAuthorSection> childAuthorSections;

    public AuthorViewFragment() {

    }

    private void bindView() {

        method = new Method(requireActivity());
        recycler_author_view = view.findViewById(R.id.recycler_author_view);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireActivity(), 3);
        recycler_author_view.setLayoutManager(gridLayoutManager);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        assert getArguments() != null;
        type = getArguments().getString("type");
        if (type.equals("author_view")) {
            title = getArguments().getString("title");
            id = getArguments().getString("id");
        }
        view = inflater.inflate(R.layout.fragment_author_view, container, false);
        bindView();
        onCreateMethod();
        return view;
    }

    private void onCreateMethod() {
        MainActivity.toolbar.setTitle(title);
        animation = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.layout_animation_fall_down);

        loadAlbums(id, recycler_author_view, new TextView(requireActivity()));
    }

    private void loadAlbums(String homeSectionId, final RecyclerView recyclerView, final TextView textView) {

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(requireActivity()));
        jsObj.addProperty("authorsection_id", homeSectionId);
        jsObj.addProperty("method_name", "author_section_id");
        jsObj.addProperty("page", "1");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<MainChildAuthorSection> call = apiService.getChildAuthorSection(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<MainChildAuthorSection>() {
            @Override
            public void onResponse(@NotNull Call<MainChildAuthorSection> call, @NotNull Response<MainChildAuthorSection> response) {

                if (getActivity() != null) {

                    if (response.isSuccessful()) {

                        MainChildAuthorSection mainChildAuthorSection = response.body();

                        childAuthorSections = new ArrayList<>();
                        childAuthorSections.addAll(mainChildAuthorSection.getChildAuthorSectionList());

                        ChildAuthorAdapter childAuthorAdapter = new ChildAuthorAdapter(childAuthorSections);
                        recyclerView.setAdapter(childAuthorAdapter);

                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<MainChildAuthorSection> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                method.alertBox(requireActivity().getResources().getString(R.string.failed_try_again));
            }
        });

    }

    private class ChildAuthorAdapter extends RecyclerView.Adapter<AuthorViewFragment.ChildAuthorAdapter.ViewHolder> {

        List<ChildAuthorSection> childAuthorSections;
        private int columnWidth;

        public ChildAuthorAdapter(List<ChildAuthorSection> childAuthorSections) {
            this.childAuthorSections = childAuthorSections;

            Resources r = requireActivity().getResources();
            float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, r.getDisplayMetrics());
            columnWidth = (int) ((method.getScreenWidth() - ((5 + 3) * padding)));
        }

        @NonNull
        @Override
        public AuthorViewFragment.ChildAuthorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new AuthorViewFragment.ChildAuthorAdapter.ViewHolder(LayoutInflater.from(requireActivity()).inflate(R.layout.author_adapter, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull AuthorViewFragment.ChildAuthorAdapter.ViewHolder holder, int position) {
            final AuthorViewFragment.ChildAuthorAdapter.ViewHolder viewHolder = holder;

            viewHolder.imageView.setLayoutParams(new ConstraintLayout.LayoutParams(columnWidth / 3, columnWidth / 3));

            viewHolder.textViewName.setText(childAuthorSections.get(position).getAuthorName());
            Glide.with(requireActivity()).load(childAuthorSections.get(position).getAuthorImage())
                    .placeholder(R.drawable.placeholder_portable)
                    .into(viewHolder.imageView);

            holder.constraintLayout.setOnClickListener(v -> {
                AuthorBookFragment authorBookFragment = new AuthorBookFragment();
                Bundle bundle = new Bundle();
                bundle.putString("title", childAuthorSections.get(position).getAuthorName());
                bundle.putString("id", childAuthorSections.get(position).getAuthorId());
                bundle.putString("type", type);
                authorBookFragment.setArguments(bundle);
                requireActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, authorBookFragment,
                        childAuthorSections.get(position).getAuthorName()).addToBackStack(childAuthorSections.get(position).getAuthorName()).commitAllowingStateLoss();
            });
        }

        @Override
        public int getItemCount() {
            return childAuthorSections.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageView;
            private ConstraintLayout constraintLayout;
            private MaterialTextView textViewName;

            public ViewHolder(View itemView) {
                super(itemView);

                imageView = itemView.findViewById(R.id.imageView_author_adapter);
                constraintLayout = itemView.findViewById(R.id.con_author_adapter);
                textViewName = itemView.findViewById(R.id.textView_authorName_adapter);

            }
        }
    }
}