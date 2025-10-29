package com.kiszka.kiddify;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.kiszka.kiddify.database.DataManager;
import com.kiszka.kiddify.databinding.ActivityProfileBinding;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ProfileActivity extends AppCompatActivity {
	private ActivityProfileBinding binding;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);
		binding = ActivityProfileBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		DataManager dm = DataManager.getInstance(this);
		String name = dm.getKidName();
		String birth = dm.getKidBirthDate();
		binding.profileName.setText(name);
		String birthFormatted = birth;
		if (birth != null && !birth.isEmpty()) {
			try {
				final String targetPattern = "dd.MM.yyyy";
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
					DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern(targetPattern);
					LocalDate parsedDate = null;
					try {
						parsedDate = LocalDate.parse(birth);
					} catch (Exception ex) {}
					if (parsedDate != null) {
						birthFormatted = parsedDate.format(outputFormatter);
					} else {
						try {
							OffsetDateTime odt = OffsetDateTime.parse(birth);
							ZonedDateTime zdt = odt.atZoneSameInstant(ZoneId.systemDefault());
							birthFormatted = zdt.format(outputFormatter);
						} catch (Exception ex) {
							try {
								ZonedDateTime zdt2 = ZonedDateTime.parse(birth);
								ZonedDateTime zdtLocal = zdt2.withZoneSameInstant(ZoneId.systemDefault());
								birthFormatted = zdtLocal.format(outputFormatter);
							} catch (Exception ex2) {}
						}
					}
				} else {
					SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
					Date date = null;
					try {
						SimpleDateFormat inputDateOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
						inputDateOnly.setTimeZone(TimeZone.getTimeZone("UTC"));
						date = inputDateOnly.parse(birth);
					} catch (Exception ex) {
						try {
							SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
							inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
							date = inputFormat.parse(birth);
						} catch (Exception ex2) {
							try {
								SimpleDateFormat inputFormatNoMs = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
								inputFormatNoMs.setTimeZone(TimeZone.getTimeZone("UTC"));
								date = inputFormatNoMs.parse(birth);
							} catch (Exception ex3) {}
						}
					}
					if (date != null) {
						birthFormatted = outputFormat.format(date);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
        binding.profileBirthDate.setText(birthFormatted);
		String formattedDate = null;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			LocalDate today = LocalDate.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", new Locale("en","US"));
			formattedDate = today.format(formatter);
		}
		binding.tvCurrentDate.setText(formattedDate);
		binding.btnLogout.setOnClickListener(v -> {
			DataManager.getInstance(ProfileActivity.this).logout();
			Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
			finish();
		});
		binding.navHome.setOnClickListener(v -> {
			Intent intent = new Intent(ProfileActivity.this, MainPageActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
			finish();
		});
		binding.navChat.setOnClickListener(v -> {
			Intent intent = new Intent(ProfileActivity.this, GalleryActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			finish();
		});
		binding.navCalendar.setOnClickListener(v -> {
			Intent intent = new Intent(ProfileActivity.this, CalendarActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
			finish();
		});
	}

}
