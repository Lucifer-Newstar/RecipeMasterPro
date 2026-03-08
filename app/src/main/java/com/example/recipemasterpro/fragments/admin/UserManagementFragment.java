package com.example.recipemasterpro.fragments.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipemasterpro.R;
import com.example.recipemasterpro.adapters.admin.UserAdminAdapter;
import com.example.recipemasterpro.models.User;
import com.example.recipemasterpro.utils.Constants;
import com.example.recipemasterpro.utils.SessionManager;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UserManagementFragment extends Fragment implements UserAdminAdapter.OnUserActionListener {

    private RecyclerView recyclerView;
    private UserAdminAdapter adapter;
    private List<User> userList;
    private ProgressBar progressBar;
    private TextView emptyText, statsText;
    private Button refreshButton;
    
    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private String currentAdminId;
    private String currentAdminName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_management, container, false);
        
        db = FirebaseFirestore.getInstance();
        sessionManager = SessionManager.getInstance(requireContext());
        currentAdminId = sessionManager.getUserId();
        currentAdminName = sessionManager.getUserName();
        
        userList = new ArrayList<>();
        
        initViews(view);
        loadUsers();
        loadStats();
        
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyText = view.findViewById(R.id.emptyText);
        statsText = view.findViewById(R.id.statsText);
        refreshButton = view.findViewById(R.id.refreshButton);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new UserAdminAdapter(getContext(), userList, this, currentAdminId);
        recyclerView.setAdapter(adapter);
        
        refreshButton.setOnClickListener(v -> {
            loadUsers();
            loadStats();
        });
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
        
        db.collection(Constants.USERS_COLLECTION)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        User user = doc.toObject(User.class);
                        user.setUserId(doc.getId());
                        userList.add(user);
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    
                    if (userList.isEmpty()) {
                        emptyText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error loading users: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadStats() {
        db.collection(Constants.USERS_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int total = queryDocumentSnapshots.size();
                    int admins = 0;
                    int chefs = 0;
                    int regular = 0;
                    
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String role = doc.getString("role");
                        if (Constants.ROLE_ADMIN.equals(role)) {
                            admins++;
                        } else if (Constants.ROLE_CHEF.equals(role)) {
                            chefs++;
                        } else {
                            regular++;
                        }
                    }
                    
                    String stats = String.format(Locale.getDefault(),
                        "📊 Total Users: %d\n👑 Admins: %d | 👨‍🍳 Chefs: %d | 👤 Users: %d",
                        total, admins, chefs, regular
                    );
                    statsText.setText(stats);
                });
    }

    @Override
    public void onUserClick(User user) {
        showUserOptionsDialog(user);
    }

    private void showUserOptionsDialog(User user) {
        boolean isSelf = user.getUserId().equals(currentAdminId);
        
        List<String> optionsList = new ArrayList<>();
        optionsList.add("View Details");
        optionsList.add("View Warnings");
        
        if (Constants.ROLE_ADMIN.equals(user.getRole())) {
            if (!isSelf) {
                optionsList.add("Demote from Admin");
            }
        } else {
            optionsList.add("Send Warning");
            optionsList.add("Make Admin");
            optionsList.add("Suspend User");
        }
        
        String[] options = optionsList.toArray(new String[0]);

        new AlertDialog.Builder(requireContext())
                .setTitle(user.getName())
                .setItems(options, (dialog, which) -> {
                    String selected = options[which];
                    switch (selected) {
                        case "View Details":
                            showUserDetails(user);
                            break;
                        case "View Warnings":
                            showUserWarnings(user);
                            break;
                        case "Demote from Admin":
                            confirmDemoteAdmin(user);
                            break;
                        case "Send Warning":
                            showSendWarningDialog(user);
                            break;
                        case "Make Admin":
                            confirmPromoteToAdmin(user);
                            break;
                        case "Suspend User":
                            showSuspendUserDialog(user);
                            break;
                    }
                })
                .show();
    }

    private void showUserDetails(User user) {
        String info = "🆔 ID: " + user.getUserId() + "\n" +
                "📧 Email: " + user.getEmail() + "\n" +
                "👤 Name: " + user.getName() + "\n" +
                "🎭 Role: " + user.getRole() + "\n" +
                "📝 Bio: " + (user.getBio() != null ? user.getBio() : "N/A") + "\n" +
                "📍 Location: " + (user.getLocation() != null ? user.getLocation() : "N/A") + "\n" +
                "📊 Recipes: " + user.getRecipeCount() + "\n" +
                "🎬 Reels: " + user.getReelCount() + "\n" +
                "👥 Followers: " + user.getFollowersCount() + "\n" +
                "👣 Following: " + user.getFollowingCount() + "\n" +
                "❤️ Total Likes: " + user.getTotalLikes() + "\n" +
                "📅 Joined: " + new java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(new java.util.Date(user.getCreatedAt())) + "\n" +
                "⏰ Last Login: " + (user.getLastLogin() > 0 ? 
                        new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                                .format(new java.util.Date(user.getLastLogin())) : "Never");

        new AlertDialog.Builder(requireContext())
                .setTitle("User Details")
                .setMessage(info)
                .setPositiveButton("OK", null)
                .show();
    }

    private void confirmPromoteToAdmin(User user) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Promote to Admin")
                .setMessage("Are you sure you want to make " + user.getName() + " an admin?")
                .setPositiveButton("Promote", (dialog, which) -> {
                    promoteToAdmin(user);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void promoteToAdmin(User user) {
        progressBar.setVisibility(View.VISIBLE);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("role", Constants.ROLE_ADMIN);
        
        db.collection(Constants.USERS_COLLECTION)
                .document(user.getUserId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), 
                            user.getName() + " is now an admin!", 
                            Toast.LENGTH_SHORT).show();
                    loadUsers();
                    loadStats();
                    
                    // Log the action
                    logAdminAction("promote", user.getUserId(), "Promoted to admin");
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to promote user", 
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmDemoteAdmin(User user) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Demote Admin")
                .setMessage("Are you sure you want to remove admin privileges from " + user.getName() + "?")
                .setPositiveButton("Demote", (dialog, which) -> {
                    demoteAdmin(user);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void demoteAdmin(User user) {
        progressBar.setVisibility(View.VISIBLE);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("role", Constants.ROLE_USER);
        
        db.collection(Constants.USERS_COLLECTION)
                .document(user.getUserId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), 
                            user.getName() + " is no longer an admin", 
                            Toast.LENGTH_SHORT).show();
                    loadUsers();
                    loadStats();
                    
                    logAdminAction("demote", user.getUserId(), "Demoted from admin");
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to demote user", 
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showSendWarningDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Send Warning to " + user.getName());

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        EditText reasonInput = new EditText(requireContext());
        reasonInput.setHint("Enter warning reason");
        reasonInput.setLines(3);
        layout.addView(reasonInput);

        builder.setView(layout);

        builder.setPositiveButton("Send Warning", (dialog, which) -> {
            String reason = reasonInput.getText().toString().trim();
            if (!reason.isEmpty()) {
                sendWarningToUser(user, reason);
            } else {
                Toast.makeText(getContext(), "Please enter a reason", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void sendWarningToUser(User user, String reason) {
        progressBar.setVisibility(View.VISIBLE);

        // Create warning object
        User.Warning warning = new User.Warning(currentAdminId, currentAdminName, reason);

        // Add warning to user's warnings array
        db.collection(Constants.USERS_COLLECTION)
                .document(user.getUserId())
                .update("warnings", FieldValue.arrayUnion(warning))
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), 
                            "Warning sent to " + user.getName(), 
                            Toast.LENGTH_SHORT).show();
                    
                    logAdminAction("warning", user.getUserId(), "Sent warning: " + reason);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to send warning", 
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showUserWarnings(User user) {
        if (user.getWarnings() == null || user.getWarnings().isEmpty()) {
            Toast.makeText(getContext(), "No warnings for this user", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder warningsText = new StringBuilder();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        for (User.Warning warning : user.getWarnings()) {
            warningsText.append("⚠️ From: ").append(warning.getAdminName()).append("\n");
            warningsText.append("📅 ").append(sdf.format(new java.util.Date(warning.getTimestamp()))).append("\n");
            warningsText.append("💬 ").append(warning.getReason()).append("\n\n");
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("User Warnings")
                .setMessage(warningsText.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private void showSuspendUserDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Suspend " + user.getName());

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        EditText reasonInput = new EditText(requireContext());
        reasonInput.setHint("Enter suspension reason");
        layout.addView(reasonInput);

        EditText daysInput = new EditText(requireContext());
        daysInput.setHint("Suspension duration (days)");
        daysInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(daysInput);

        builder.setView(layout);

        builder.setPositiveButton("Suspend", (dialog, which) -> {
            String reason = reasonInput.getText().toString().trim();
            String daysStr = daysInput.getText().toString().trim();
            
            if (reason.isEmpty() || daysStr.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int days = Integer.parseInt(daysStr);
            suspendUser(user, reason, days);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void suspendUser(User user, String reason, int days) {
        progressBar.setVisibility(View.VISIBLE);

        long suspendedUntil = System.currentTimeMillis() + (days * 24L * 60L * 60L * 1000L);

        Map<String, Object> updates = new HashMap<>();
        updates.put("isSuspended", true);
        updates.put("suspendedUntil", suspendedUntil);
        updates.put("suspensionReason", reason);

        db.collection(Constants.USERS_COLLECTION)
                .document(user.getUserId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), 
                            user.getName() + " suspended for " + days + " days", 
                            Toast.LENGTH_SHORT).show();
                    
                    logAdminAction("suspend", user.getUserId(), 
                            "Suspended for " + days + " days: " + reason);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to suspend user", 
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void logAdminAction(String action, String targetId, String details) {
        Map<String, Object> log = new HashMap<>();
        log.put("adminId", currentAdminId);
        log.put("adminName", currentAdminName);
        log.put("action", action);
        log.put("targetId", targetId);
        log.put("details", details);
        log.put("timestamp", System.currentTimeMillis());

        db.collection("admin_logs")
                .add(log);
    }

    @Override
    public void onUserSuspend(User user) {
        if (user.getUserId().equals(currentAdminId)) {
            Toast.makeText(getContext(), "You cannot suspend yourself", Toast.LENGTH_SHORT).show();
            return;
        }
        showSuspendUserDialog(user);
    }

    @Override
    public void onUserDelete(User user) {
        if (user.getUserId().equals(currentAdminId)) {
            Toast.makeText(getContext(), "You cannot delete yourself", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete User")
                .setMessage("Are you sure you want to permanently delete " + user.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteUser(user);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(User user) {
        progressBar.setVisibility(View.VISIBLE);
        
        db.collection(Constants.USERS_COLLECTION)
                .document(user.getUserId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "User deleted", Toast.LENGTH_SHORT).show();
                    loadUsers();
                    loadStats();
                    
                    logAdminAction("delete", user.getUserId(), "User deleted");
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to delete user", 
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onUserPromote(User user) {
        confirmPromoteToAdmin(user);
    }
}
