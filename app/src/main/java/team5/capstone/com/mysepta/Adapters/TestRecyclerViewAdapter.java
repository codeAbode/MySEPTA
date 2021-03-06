package team5.capstone.com.mysepta.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import team5.capstone.com.mysepta.R;

/**
 * Adapter created for test purposes.
 * Created by Andrew on 9/19/2015.
 */
public class TestRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Object> contents;

    static final int TYPE_HEADER = 0;
    static final int TYPE_CELL = 1;
    Context context;

    /**
     * Constructor
     * @param contents list of items
     * @param context activity
     */
    public TestRecyclerViewAdapter(List<Object> contents, Context context) {
        this.context = context;
        this.contents = contents;
    }

    /**
     * Return view type
     * @param position item position
     * @return TYPE_HEADER if position 0, otherwise TYPE_CELL
     */
    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case 0:
                return TYPE_HEADER;
            default:
                return TYPE_CELL;
        }
    }

    /**
     * Get item count
     * @return size of contents
     */
    @Override
    public int getItemCount() {
        return contents.size();
    }

    /**
     * Create view holder and set onclicklistener
     * @param parent parent viewgroup
     * @param viewType type of item view
     * @return view holder
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;

        switch (viewType) {
            case TYPE_HEADER: {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_card_big, parent, false);
                return new RecyclerView.ViewHolder(view) {
                };
            }
            case TYPE_CELL: {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_card_small, parent, false);
                return new RecyclerView.ViewHolder(view) {
                };
            }
        }
        return null;
    }


    /**
     * Initialize view holder
     * @param holder current view holder
     * @param position current item position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_HEADER:
                break;
            case TYPE_CELL:
                break;
        }
    }
}