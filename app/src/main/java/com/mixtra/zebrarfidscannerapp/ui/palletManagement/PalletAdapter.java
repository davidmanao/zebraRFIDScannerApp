package com.mixtra.zebrarfidscannerapp.ui.palletManagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mixtra.zebrarfidscannerapp.R;
import com.mixtra.zebrarfidscannerapp.api.model.PalletListResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PalletAdapter extends RecyclerView.Adapter<PalletAdapter.PalletViewHolder> {

    private List<PalletListResponse.PalletData> palletList;
    private OnPalletClickListener clickListener;

    public interface OnPalletClickListener {
        void onPalletClick(PalletListResponse.PalletData pallet);
    }

    public PalletAdapter() {
        this.palletList = new ArrayList<>();
    }

    public void setPalletList(List<PalletListResponse.PalletData> palletList) {
        this.palletList = palletList != null ? palletList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnPalletClickListener(OnPalletClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public PalletViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pallet, parent, false);
        return new PalletViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PalletViewHolder holder, int position) {
        PalletListResponse.PalletData pallet = palletList.get(position);
        holder.bind(pallet);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onPalletClick(pallet);
            }
        });
    }

    @Override
    public int getItemCount() {
        return palletList.size();
    }

    public static class PalletViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPalletCode;
        private TextView tvPalletType;
        private TextView tvPalletName;
        private TextView tvCapacity;
        private TextView tvBalance;
        private TextView tvTare;
        private TextView tvInitial;
        private TextView tvIncoming;
        private TextView tvOutgoing;
        private TextView tvRfidTag;
        private TextView tvDateIn;
        private TextView tvUserIn;

        public PalletViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPalletCode = itemView.findViewById(R.id.tv_pallet_code);
            tvPalletType = itemView.findViewById(R.id.tv_pallet_type);
            tvPalletName = itemView.findViewById(R.id.tv_pallet_name);
            tvCapacity = itemView.findViewById(R.id.tv_capacity);
            tvBalance = itemView.findViewById(R.id.tv_balance);
            tvTare = itemView.findViewById(R.id.tv_tare);
            tvInitial = itemView.findViewById(R.id.tv_initial);
            tvIncoming = itemView.findViewById(R.id.tv_incoming);
            tvOutgoing = itemView.findViewById(R.id.tv_outgoing);
            tvRfidTag = itemView.findViewById(R.id.tv_rfid_tag);
            tvDateIn = itemView.findViewById(R.id.tv_date_in);
            tvUserIn = itemView.findViewById(R.id.tv_user_in);
        }

        public void bind(PalletListResponse.PalletData pallet) {
            // Set basic info
            tvPalletCode.setText(pallet.getCode() != null ? pallet.getCode() : "N/A");
            tvPalletType.setText(pallet.getType() != null ? pallet.getType() : "Unknown");
            tvPalletName.setText(pallet.getName() != null ? pallet.getName() : "N/A");

            // Set capacity information
            tvCapacity.setText(String.valueOf(pallet.getCapacity() != null ? pallet.getCapacity() : 0));
            tvBalance.setText(String.valueOf(pallet.getBalance() != null ? pallet.getBalance() : 0));
            tvTare.setText(String.valueOf(pallet.getTare() != null ? pallet.getTare() : 0));

            // Set movement information
            tvInitial.setText(String.valueOf(pallet.getInitial() != null ? pallet.getInitial() : 0));
            tvIncoming.setText(String.valueOf(pallet.getIncoming() != null ? pallet.getIncoming() : 0));
            tvOutgoing.setText(String.valueOf(pallet.getOutgoing() != null ? pallet.getOutgoing() : 0));

            // Set RFID tag
            String rfidTag = pallet.getRfidTag();
            if (rfidTag != null && !rfidTag.trim().isEmpty()) {
                tvRfidTag.setText(rfidTag);
            } else {
                tvRfidTag.setText("No RFID Tag");
                tvRfidTag.setTextColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
            }

            // Format and set date
            String dateIn = pallet.getDateIn();
            if (dateIn != null && !dateIn.trim().isEmpty()) {
                try {
                    // Parse ISO 8601 date format
                    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    Date date = inputFormat.parse(dateIn);
                    tvDateIn.setText(outputFormat.format(date));
                } catch (Exception e) {
                    // If parsing fails, just show the raw date
                    tvDateIn.setText(dateIn.substring(0, Math.min(10, dateIn.length())));
                }
            } else {
                tvDateIn.setText("N/A");
            }

            // Set user info
            tvUserIn.setText(pallet.getUserIn() != null ? pallet.getUserIn() : "N/A");

            // Set type background color based on type
            if (pallet.getType() != null) {
                switch (pallet.getType().toLowerCase()) {
                    case "plastic":
                        tvPalletType.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.purple_500));
                        break;
                    case "wood":
                        tvPalletType.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.teal_700));
                        break;
                    case "metal":
                        tvPalletType.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
                        break;
                    default:
                        tvPalletType.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.purple_500));
                        break;
                }
            }
        }
    }
}