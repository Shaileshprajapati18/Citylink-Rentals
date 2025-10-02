package com.example.citylinkrentals.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.citylinkrentals.R;
import com.example.citylinkrentals.model.MockPrediction;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying place predictions in the autocomplete search
 */
public class PlacePredictionAdapter extends RecyclerView.Adapter<PlacePredictionAdapter.PredictionViewHolder> {

    private List<AutocompletePrediction> predictions = new ArrayList<>();
    private OnPlaceClickListener onPlaceClickListener;

    // Listener interface for item clicks
    public interface OnPlaceClickListener {
        void onPlaceClick(AutocompletePrediction prediction);
    }

    public PlacePredictionAdapter(OnPlaceClickListener listener) {
        this.onPlaceClickListener = listener;
    }

    @NonNull
    @Override
    public PredictionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place_prediction, parent, false);
        return new PredictionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PredictionViewHolder holder, int position) {
        AutocompletePrediction prediction = predictions.get(position);
        holder.bind(prediction);
    }

    @Override
    public int getItemCount() {
        return usingMock ? mockPredictions.size() : predictions.size();
    }


    private List<MockPrediction> mockPredictions = new ArrayList<>();
    private boolean usingMock = false;

    public void updateMockPredictions(List<MockPrediction> predictions) {
        usingMock = true;
        mockPredictions.clear();
        mockPredictions.addAll(predictions);
        notifyDataSetChanged();
    }

    // Method to update the prediction list
    public void updatePredictions(List<AutocompletePrediction> newPredictions) {
        this.predictions.clear();
        this.predictions.addAll(newPredictions);
        notifyDataSetChanged();
    }

    class PredictionViewHolder extends RecyclerView.ViewHolder {
        private ImageView placeIcon;
        private TextView primaryText;
        private TextView secondaryText;

        public PredictionViewHolder(@NonNull View itemView) {
            super(itemView);
            placeIcon = itemView.findViewById(R.id.place_icon);
            primaryText = itemView.findViewById(R.id.primary_text);
            secondaryText = itemView.findViewById(R.id.secondary_text);

            // Handle click on each item
            itemView.setOnClickListener(v -> {
                if (onPlaceClickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    onPlaceClickListener.onPlaceClick(predictions.get(getAdapterPosition()));
                }
            });
        }

        public void bind(AutocompletePrediction prediction) {
            // Set main place name
            primaryText.setText(prediction.getPrimaryText(null));

            // Set address details if available
            CharSequence secondary = prediction.getSecondaryText(null);
            if (secondary != null && secondary.length() > 0) {
                secondaryText.setText(secondary);
                secondaryText.setVisibility(View.VISIBLE);
            } else {
                secondaryText.setVisibility(View.GONE);
            }

            // Set appropriate icon
            setPlaceIcon(prediction);
        }

        private void setPlaceIcon(AutocompletePrediction prediction) {
            List<Place.Type> placeTypes = prediction.getPlaceTypes();

            if (placeTypes != null && !placeTypes.isEmpty()) {
                Place.Type primaryType = placeTypes.get(0);

                switch (primaryType) {
                    case RESTAURANT:
                    case FOOD:
                    case MEAL_TAKEAWAY:
                    case MEAL_DELIVERY:
                        placeIcon.setImageResource(R.drawable.restaurant_24px);
                        break;
                    case GAS_STATION:
                        placeIcon.setImageResource(R.drawable.local_gas_station_24px);
                        break;
                    case HOSPITAL:
                    case PHARMACY:
                        placeIcon.setImageResource(R.drawable.local_hospital_24px);
                        break;
                    case SCHOOL:
                    case UNIVERSITY:
                        placeIcon.setImageResource(R.drawable.school_24px);
                        break;
                    case BANK:
                    case ATM:
                        placeIcon.setImageResource(R.drawable.account_balance_24px);
                        break;
                    case SHOPPING_MALL:
                    case STORE:
                        placeIcon.setImageResource(R.drawable.shopping_cart_24px);
                        break;
                    case LODGING:
                        placeIcon.setImageResource(R.drawable.pg); // Ensure pg.png exists in drawable
                        break;
                    case TOURIST_ATTRACTION:
                        placeIcon.setImageResource(R.drawable.location_on_24px);
                        break;
                    case TRANSIT_STATION:
                    case BUS_STATION:
                    case SUBWAY_STATION:
                        placeIcon.setImageResource(R.drawable.directions_bus_24px);
                        break;
                    default:
                        // Use location icon for unknown types or fallback
                        if (isSpecificAddress(prediction)) {
                            placeIcon.setImageResource(R.drawable.location_on_24px);
                        } else {
                            placeIcon.setImageResource(R.drawable.location_on_24px);
                        }
                        break;
                }
            } else {
                // Default icon if no type found
                placeIcon.setImageResource(R.drawable.location_on_24px);
            }
        }

        // Utility: check if it's likely a specific address (has numbers)
        private boolean isSpecificAddress(AutocompletePrediction prediction) {
            String primaryText = prediction.getPrimaryText(null).toString();
            return primaryText.matches(".*\\d+.*");
        }
    }
}
