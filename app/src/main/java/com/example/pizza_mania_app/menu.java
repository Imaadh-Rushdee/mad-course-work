package com.example.pizza_mania_app;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class menu extends AppCompatActivity {

    private LinearLayout menuContainer;
    private SQLiteDatabase db;
    private String userRole;
    private Button btnNewItem;

    private static final int REQUEST_CAMERA = 100;
    private static final int REQUEST_GALLERY = 200;
    private ImageView tempImageView;   // to preview image inside dialog
    private byte[] selectedImageBytes; // store image as bytes temporarily

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        menuContainer = findViewById(R.id.menuContainer);
        btnNewItem = findViewById(R.id.btnNewItem);

        // Get user role from login (e.g., "admin" or "user")
        userRole = getIntent().getStringExtra("userRole");

        db = openOrCreateDatabase("pizza_mania.db", MODE_PRIVATE, null);
        createMenuTableIfNotExists();
        insertSampleMenuItems();
        loadMenuItems();

        // Show "Add New Item" button only for admin
        if ("admin".equals(userRole)) {
            btnNewItem.setVisibility(View.VISIBLE);
            btnNewItem.setOnClickListener(v -> showAddOrEditDialog(null, null, 0, 0, null));
        } else {
            btnNewItem.setVisibility(View.GONE);
        }
    }

    private void createMenuTableIfNotExists() {
        db.execSQL("CREATE TABLE IF NOT EXISTS menu_items (" +
                "item_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "description TEXT, " +
                "price REAL, " +
                "image BLOB)");
    }

    private void insertSampleMenuItems() {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM menu_items", null);
        if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
            ContentValues values = new ContentValues();
            values.put("name", "Margherita Pizza");
            values.put("description", "Classic cheese & tomato");
            values.put("price", 1200);
            db.insert("menu_items", null, values);

            values = new ContentValues();
            values.put("name", "Pepperoni Pizza");
            values.put("description", "Loaded with pepperoni");
            values.put("price", 1500);
            db.insert("menu_items", null, values);

            values = new ContentValues();
            values.put("name", "BBQ Chicken Pizza");
            values.put("description", "Chicken + BBQ sauce");
            values.put("price", 1800);
            db.insert("menu_items", null, values);
        }
        cursor.close();
    }

    private void loadMenuItems() {
        menuContainer.removeAllViews();
        Cursor cursor = db.rawQuery("SELECT * FROM menu_items", null);

        while (cursor.moveToNext()) {
            int itemId = cursor.getInt(cursor.getColumnIndexOrThrow("item_id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String desc = cursor.getString(cursor.getColumnIndexOrThrow("description"));
            double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
            byte[] imageBytes = cursor.getBlob(cursor.getColumnIndexOrThrow("image"));

            addMenuItem(itemId, name, desc, price, imageBytes);
        }

        cursor.close();
    }

    private void addMenuItem(int itemId, String name, String desc, double price, byte[] imageBytes) {
        LinearLayout card = (LinearLayout) getLayoutInflater()
                .inflate(R.layout.menu_item, menuContainer, false);

        TextView tvName = card.findViewById(R.id.itemName);
        TextView tvDesc = card.findViewById(R.id.itemDesc);
        TextView tvPrice = card.findViewById(R.id.itemPrice);
        ImageView img = card.findViewById(R.id.itemImage);
        Button btnAdd = card.findViewById(R.id.btnAdd);

        tvName.setText(name);
        tvDesc.setText(desc);
        tvPrice.setText("Rs. " + price);

        if (imageBytes != null) {
            Bitmap bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            img.setImageBitmap(bmp);
        } else {
            img.setImageResource(R.drawable.logo); // fallback
        }

        btnAdd.setOnClickListener(v ->
                Toast.makeText(this, name + " added to cart!", Toast.LENGTH_SHORT).show()
        );

        // Admin controls: Edit & Remove buttons
        Button btnEdit = card.findViewById(R.id.btnEdit);
        Button btnRemove = card.findViewById(R.id.btnRemove);

        if ("admin".equals(userRole)) {
            btnEdit.setVisibility(View.VISIBLE);
            btnRemove.setVisibility(View.VISIBLE);

            btnEdit.setOnClickListener(v ->
                    showAddOrEditDialog(name, desc, price, itemId, imageBytes)
            );

            btnRemove.setOnClickListener(v -> {
                db.delete("menu_items", "item_id=?", new String[]{String.valueOf(itemId)});
                Toast.makeText(this, name + " removed!", Toast.LENGTH_SHORT).show();
                loadMenuItems();
            });

        } else {
            btnEdit.setVisibility(View.GONE);
            btnRemove.setVisibility(View.GONE);
        }

        menuContainer.addView(card);
    }

    // 🔹 Add or Edit dialog (shared)
    private void showAddOrEditDialog(String oldName, String oldDesc, double oldPrice, Integer itemId, byte[] oldImage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(itemId == null ? "Add New Item" : "Edit Item");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 30, 40, 10);

        final EditText etName = new EditText(this);
        etName.setHint("Item Name");
        if (oldName != null) etName.setText(oldName);
        layout.addView(etName);

        final EditText etDesc = new EditText(this);
        etDesc.setHint("Description");
        if (oldDesc != null) etDesc.setText(oldDesc);
        layout.addView(etDesc);

        final EditText etPrice = new EditText(this);
        etPrice.setHint("Price");
        etPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        if (oldPrice > 0) etPrice.setText(String.valueOf(oldPrice));
        layout.addView(etPrice);

        tempImageView = new ImageView(this);
        tempImageView.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
        tempImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (oldImage != null) {
            Bitmap bmp = BitmapFactory.decodeByteArray(oldImage, 0, oldImage.length);
            tempImageView.setImageBitmap(bmp);
            selectedImageBytes = oldImage;
        }
        layout.addView(tempImageView);

        Button btnImage = new Button(this);
        btnImage.setText("Choose Image");
        btnImage.setOnClickListener(v -> showImagePicker());
        layout.addView(btnImage);

        builder.setView(layout);

        builder.setPositiveButton(itemId == null ? "Add" : "Update", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String desc = etDesc.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();

            if (name.isEmpty() || desc.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double price = Double.parseDouble(priceStr);

            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("description", desc);
            values.put("price", price);
            if (selectedImageBytes != null) values.put("image", selectedImageBytes);

            if (itemId == null) {
                db.insert("menu_items", null, values);
                Toast.makeText(this, "Item added: " + name, Toast.LENGTH_SHORT).show();
            } else {
                db.update("menu_items", values, "item_id=?", new String[]{String.valueOf(itemId)});
                Toast.makeText(this, "Item updated: " + name, Toast.LENGTH_SHORT).show();
            }

            selectedImageBytes = null; // reset
            loadMenuItems();
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // 🔹 Choose image (camera/gallery)
    private void showImagePicker() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) { // Camera
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_CAMERA);
            } else { // Gallery
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_GALLERY);
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Bitmap bitmap = null;
            if (requestCode == REQUEST_CAMERA) {
                bitmap = (Bitmap) data.getExtras().get("data");
            } else if (requestCode == REQUEST_GALLERY) {
                Uri uri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (bitmap != null) {
                tempImageView.setImageBitmap(bitmap);
                selectedImageBytes = bitmapToBytes(bitmap);
            }
        }
    }

    // Convert bitmap -> byte[]
    private byte[] bitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }
}
