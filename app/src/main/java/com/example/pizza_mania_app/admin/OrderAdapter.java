package com.example.pizza_mania_app.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pizza_mania_app.R;
import com.example.pizza_mania_app.OrderDatabaseHelper;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private OrderDatabaseHelper dbHelper;

    public OrderAdapter(List<Order> orders, OrderDatabaseHelper dbHelper) {
        this.orderList = orders;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_pending, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.tvOrderId.setText("ID: " + order.getId());
        holder.tvOrderName.setText(order.getName());
        holder.tvOrderStatus.setText(order.getStatus());

        holder.btnReady.setOnClickListener(v -> {
            dbHelper.updateOrderStatus(order.getId(), "Completed");
            order.setStatus("Completed");
            notifyItemChanged(position);
        });

        holder.btnReject.setOnClickListener(v -> {
            dbHelper.deleteOrder(order.getId());
            orderList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, orderList.size());
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void updateList(List<Order> orders) {
        this.orderList = orders;
        notifyDataSetChanged();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderName, tvOrderStatus;
        Button btnReady, btnReject;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderName = itemView.findViewById(R.id.tvOrderName);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            btnReady = itemView.findViewById(R.id.btnOrderReady);
            btnReject = itemView.findViewById(R.id.btnRejectOrder);
        }
    }
}
