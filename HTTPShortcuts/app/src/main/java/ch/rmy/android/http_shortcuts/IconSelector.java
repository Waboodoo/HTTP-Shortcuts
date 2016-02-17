package ch.rmy.android.http_shortcuts;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * 
 * A dialog window that lists all built-in icons, from which the user can select one.
 * 
 * @author Roland Meyer
 * 
 */
public class IconSelector extends Dialog {

	private static final int[] ICONS = { R.drawable.circle_blue, R.drawable.circle_cyan, R.drawable.circle_green, R.drawable.circle_magenta, R.drawable.circle_orange,
			R.drawable.circle_purple, R.drawable.circle_red, R.drawable.circle_yellow, R.drawable.black_battery_charging, R.drawable.black_box, R.drawable.black_alarm_clock,
			R.drawable.black_clock, R.drawable.black_calendar_date, R.drawable.black_camera, R.drawable.black_cctv_camera, R.drawable.black_car,
			R.drawable.black_transport_school_bus, R.drawable.black_cycling, R.drawable.black_spaceship, R.drawable.black_cloud, R.drawable.black_color,
			R.drawable.black_command_refresh, R.drawable.black_cursor, R.drawable.black_clean, R.drawable.black_data_edit, R.drawable.black_data_information,
			R.drawable.black_device_bell, R.drawable.black_device_calculator, R.drawable.black_dice, R.drawable.black_display_brightness, R.drawable.black_display_contrast,
			R.drawable.black_document, R.drawable.black_business_man, R.drawable.black_user_female, R.drawable.black_user_male, R.drawable.black_robot, R.drawable.black_view,
			R.drawable.black_butterfly, R.drawable.black_cat, R.drawable.black_dog, R.drawable.black_fish, R.drawable.black_squirrel, R.drawable.black_turtle,
			R.drawable.black_penguin, R.drawable.black_footprint, R.drawable.black_foot_print, R.drawable.black_flower, R.drawable.black_pine_tree, R.drawable.black_palm_tree,
			R.drawable.black_fire, R.drawable.black_folder_sharing, R.drawable.black_fuel_station, R.drawable.black_gift, R.drawable.black_globe, R.drawable.black_graph,
			R.drawable.black_hand, R.drawable.black_heart, R.drawable.black_help, R.drawable.black_hour_glass, R.drawable.black_house, R.drawable.black_houses,
			R.drawable.black_office, R.drawable.black_university, R.drawable.black_tent, R.drawable.black_sofa, R.drawable.black_identity_card,
			R.drawable.black_instrument_barometer, R.drawable.black_instrument_round_bottom_flask, R.drawable.black_instrument_telescope, R.drawable.black_iris,
			R.drawable.black_solar, R.drawable.black_moon, R.drawable.black_energy_saving_bulb_1, R.drawable.black_energy_saving_bulb_2, R.drawable.black_light,
			R.drawable.black_light_bulb, R.drawable.black_torch, R.drawable.black_linear_gauge, R.drawable.black_driller, R.drawable.black_key_access, R.drawable.black_lock,
			R.drawable.black_latitude, R.drawable.black_magnet, R.drawable.black_map_location, R.drawable.black_media_back, R.drawable.black_media_end,
			R.drawable.black_media_fast_forward, R.drawable.black_media_pause, R.drawable.black_media_play, R.drawable.black_messages, R.drawable.black_plus,
			R.drawable.black_minus, R.drawable.black_movie, R.drawable.black_new, R.drawable.black_next, R.drawable.black_outlet_plug, R.drawable.black_phone,
			R.drawable.black_photograph, R.drawable.black_picture, R.drawable.black_picture_frame, R.drawable.black_planet, R.drawable.black_plug, R.drawable.black_power_1,
			R.drawable.black_power_2, R.drawable.black_printer, R.drawable.black_printer_2, R.drawable.black_puzzles, R.drawable.black_left_arrow, R.drawable.black_reload,
			R.drawable.black_road_backward, R.drawable.black_road_forward, R.drawable.black_circle, R.drawable.black_rectangle, R.drawable.black_shape12, R.drawable.black_shape6,
			R.drawable.black_shape_cube, R.drawable.black_shape_peace, R.drawable.black_shape_spade, R.drawable.black_shape_square, R.drawable.black_shape_square2,
			R.drawable.black_shape_star, R.drawable.black_shape_triangle, R.drawable.black_tube, R.drawable.black_smile, R.drawable.black_laugh, R.drawable.black_sad,
			R.drawable.black_sleepy, R.drawable.black_smoking_zone, R.drawable.black_check, R.drawable.black_close, R.drawable.black_synchronize, R.drawable.black_tag,
			R.drawable.black_target, R.drawable.black_toolbox, R.drawable.black_tools, R.drawable.black_headphone, R.drawable.black_gramaphone, R.drawable.black_guitar,
			R.drawable.black_recorded_media, R.drawable.black_beamed_notes, R.drawable.black_quarter_note, R.drawable.black_microphone, R.drawable.black_tape_recorder,
			R.drawable.black_loud_speaker, R.drawable.black_voicemail, R.drawable.black_speaker_volume, R.drawable.black_speech, R.drawable.black_volume_mute,
			R.drawable.black_volume_high, R.drawable.black_volume_low, R.drawable.black_announcement, R.drawable.black_waiting_popup, R.drawable.black_warning_message,
			R.drawable.black_refrigerator, R.drawable.black_washing_machine, R.drawable.black_internet, R.drawable.black_bluetooth, R.drawable.black_beep_signal,
			R.drawable.black_network_sharing, R.drawable.black_lan, R.drawable.black_microsoft_windows, R.drawable.black_windows_pc, R.drawable.black_linux,
			R.drawable.black_mac_pc, R.drawable.black_command_mac, R.drawable.black_laptop, R.drawable.black_ipad, R.drawable.black_mobile_phone, R.drawable.black_telephone,
			R.drawable.black_webcam, R.drawable.black_usb, R.drawable.black_mail, R.drawable.black_satellite, R.drawable.black_cd, R.drawable.black_dvd, R.drawable.black_keyboard,
			R.drawable.black_mouse, R.drawable.black_joystick, R.drawable.black_film, R.drawable.black_feather, R.drawable.black_water_tap, R.drawable.black_weather,
			R.drawable.black_umbrella, R.drawable.black_thunder, R.drawable.black_sports, R.drawable.black_bat_man, R.drawable.black_armor, R.drawable.black_sword,
			R.drawable.black_attachment, R.drawable.black_balloon, R.drawable.black_search, R.drawable.black_settings, R.drawable.black_yin_yang, R.drawable.black_cooker,
			R.drawable.black_cutlery_fork_knife, R.drawable.black_snack_doughnut, R.drawable.black_beverage_coffee, R.drawable.black_cake, R.drawable.black_cheese,
			R.drawable.black_chef_cap, R.drawable.black_bell, R.drawable.black_book_open, R.drawable.black_danger, R.drawable.black_delete, R.drawable.black_finger_print,
			R.drawable.black_floppy, R.drawable.black_hat, R.drawable.black_knight, R.drawable.black_link, R.drawable.black_logout, R.drawable.black_password,
			R.drawable.black_shape_lightning, R.drawable.black_button, R.drawable.black_swap_down, R.drawable.black_swap_left, R.drawable.black_swap_right,
			R.drawable.black_swap_up, R.drawable.black_table_fan, R.drawable.black_video_camera, R.drawable.black_table_lamp, R.drawable.black_remote_control,
			R.drawable.black_television, R.drawable.black_toy, R.drawable.grey_battery_charging, R.drawable.grey_box, R.drawable.grey_alarm_clock, R.drawable.grey_clock,
			R.drawable.grey_calendar_date, R.drawable.grey_camera, R.drawable.grey_cctv_camera, R.drawable.grey_car, R.drawable.grey_transport_school_bus, R.drawable.grey_cycling,
			R.drawable.grey_spaceship, R.drawable.grey_cloud, R.drawable.grey_color, R.drawable.grey_command_refresh, R.drawable.grey_cursor, R.drawable.grey_clean,
			R.drawable.grey_data_edit, R.drawable.grey_data_information, R.drawable.grey_device_bell, R.drawable.grey_device_calculator, R.drawable.grey_dice,
			R.drawable.grey_display_brightness, R.drawable.grey_display_contrast, R.drawable.grey_document, R.drawable.grey_business_man, R.drawable.grey_user_female,
			R.drawable.grey_user_male, R.drawable.grey_robot, R.drawable.grey_view, R.drawable.grey_butterfly, R.drawable.grey_cat, R.drawable.grey_dog, R.drawable.grey_fish,
			R.drawable.grey_squirrel, R.drawable.grey_turtle, R.drawable.grey_penguin, R.drawable.grey_footprint, R.drawable.grey_foot_print, R.drawable.grey_flower,
			R.drawable.grey_pine_tree, R.drawable.grey_palm_tree, R.drawable.grey_fire, R.drawable.grey_folder_sharing, R.drawable.grey_fuel_station, R.drawable.grey_gift,
			R.drawable.grey_globe, R.drawable.grey_graph, R.drawable.grey_hand, R.drawable.grey_heart, R.drawable.grey_help, R.drawable.grey_hour_glass, R.drawable.grey_house,
			R.drawable.grey_houses, R.drawable.grey_office, R.drawable.grey_university, R.drawable.grey_tent, R.drawable.grey_sofa, R.drawable.grey_identity_card,
			R.drawable.grey_instrument_barometer, R.drawable.grey_instrument_round_bottom_flask, R.drawable.grey_instrument_telescope, R.drawable.grey_iris, R.drawable.grey_solar,
			R.drawable.grey_moon, R.drawable.grey_energy_saving_bulb_1, R.drawable.grey_energy_saving_bulb_2, R.drawable.grey_light, R.drawable.grey_light_bulb,
			R.drawable.grey_torch, R.drawable.grey_linear_gauge, R.drawable.grey_driller, R.drawable.grey_key_access, R.drawable.grey_lock, R.drawable.grey_latitude,
			R.drawable.grey_magnet, R.drawable.grey_map_location, R.drawable.grey_media_back, R.drawable.grey_media_end, R.drawable.grey_media_fast_forward,
			R.drawable.grey_media_pause, R.drawable.grey_media_play, R.drawable.grey_messages, R.drawable.grey_plus, R.drawable.grey_minus, R.drawable.grey_movie,
			R.drawable.grey_new, R.drawable.grey_next, R.drawable.grey_outlet_plug, R.drawable.grey_phone, R.drawable.grey_photograph, R.drawable.grey_picture,
			R.drawable.grey_picture_frame, R.drawable.grey_planet, R.drawable.grey_plug, R.drawable.grey_power_1, R.drawable.grey_power_2, R.drawable.grey_printer,
			R.drawable.grey_printer_2, R.drawable.grey_puzzles, R.drawable.grey_left_arrow, R.drawable.grey_reload, R.drawable.grey_road_backward, R.drawable.grey_road_forward,
			R.drawable.grey_circle, R.drawable.grey_rectangle, R.drawable.grey_shape12, R.drawable.grey_shape6, R.drawable.grey_shape_cube, R.drawable.grey_shape_peace,
			R.drawable.grey_shape_spade, R.drawable.grey_shape_square, R.drawable.grey_shape_square2, R.drawable.grey_shape_star, R.drawable.grey_shape_triangle,
			R.drawable.grey_tube, R.drawable.grey_smile, R.drawable.grey_laugh, R.drawable.grey_sad, R.drawable.grey_sleepy, R.drawable.grey_smoking_zone, R.drawable.grey_check,
			R.drawable.grey_close, R.drawable.grey_synchronize, R.drawable.grey_tag, R.drawable.grey_target, R.drawable.grey_toolbox, R.drawable.grey_tools,
			R.drawable.grey_headphone, R.drawable.grey_gramaphone, R.drawable.grey_guitar, R.drawable.grey_recorded_media, R.drawable.grey_beamed_notes,
			R.drawable.grey_quarter_note, R.drawable.grey_microphone, R.drawable.grey_tape_recorder, R.drawable.grey_loud_speaker, R.drawable.grey_voicemail,
			R.drawable.grey_speaker_volume, R.drawable.grey_speech, R.drawable.grey_volume_mute, R.drawable.grey_volume_high, R.drawable.grey_volume_low,
			R.drawable.grey_announcement, R.drawable.grey_waiting_popup, R.drawable.grey_warning_message, R.drawable.grey_refrigerator, R.drawable.grey_washing_machine,
			R.drawable.grey_internet, R.drawable.grey_bluetooth, R.drawable.grey_beep_signal, R.drawable.grey_network_sharing, R.drawable.grey_lan,
			R.drawable.grey_microsoft_windows, R.drawable.grey_windows_pc, R.drawable.grey_linux, R.drawable.grey_mac_pc, R.drawable.grey_command_mac, R.drawable.grey_laptop,
			R.drawable.grey_ipad, R.drawable.grey_mobile_phone, R.drawable.grey_telephone, R.drawable.grey_webcam, R.drawable.grey_usb, R.drawable.grey_mail,
			R.drawable.grey_satellite, R.drawable.grey_cd, R.drawable.grey_dvd, R.drawable.grey_keyboard, R.drawable.grey_mouse, R.drawable.grey_joystick, R.drawable.grey_film,
			R.drawable.grey_feather, R.drawable.grey_water_tap, R.drawable.grey_weather, R.drawable.grey_umbrella, R.drawable.grey_thunder, R.drawable.grey_sports,
			R.drawable.grey_bat_man, R.drawable.grey_armor, R.drawable.grey_sword, R.drawable.grey_attachment, R.drawable.grey_balloon, R.drawable.grey_search,
			R.drawable.grey_settings, R.drawable.grey_yin_yang, R.drawable.grey_cooker, R.drawable.grey_cutlery_fork_knife, R.drawable.grey_snack_doughnut,
			R.drawable.grey_beverage_coffee, R.drawable.grey_cake, R.drawable.grey_cheese, R.drawable.grey_chef_cap, R.drawable.grey_bell, R.drawable.grey_book_open,
			R.drawable.grey_danger, R.drawable.grey_delete, R.drawable.grey_finger_print, R.drawable.grey_floppy, R.drawable.grey_hat, R.drawable.grey_knight,
			R.drawable.grey_link, R.drawable.grey_logout, R.drawable.grey_password, R.drawable.grey_shape_lightning, R.drawable.grey_button, R.drawable.grey_swap_down,
			R.drawable.grey_swap_left, R.drawable.grey_swap_right, R.drawable.grey_swap_up, R.drawable.grey_table_fan, R.drawable.grey_video_camera, R.drawable.grey_table_lamp,
			R.drawable.grey_remote_control, R.drawable.grey_television, R.drawable.grey_toy, R.drawable.white_battery_charging, R.drawable.white_box, R.drawable.white_alarm_clock,
			R.drawable.white_clock, R.drawable.white_calendar_date, R.drawable.white_camera, R.drawable.white_cctv_camera, R.drawable.white_car,
			R.drawable.white_transport_school_bus, R.drawable.white_cycling, R.drawable.white_spaceship, R.drawable.white_cloud, R.drawable.white_color,
			R.drawable.white_command_refresh, R.drawable.white_cursor, R.drawable.white_clean, R.drawable.white_data_edit, R.drawable.white_data_information,
			R.drawable.white_device_bell, R.drawable.white_device_calculator, R.drawable.white_dice, R.drawable.white_display_brightness, R.drawable.white_display_contrast,
			R.drawable.white_document, R.drawable.white_business_man, R.drawable.white_user_female, R.drawable.white_user_male, R.drawable.white_robot, R.drawable.white_view,
			R.drawable.white_butterfly, R.drawable.white_cat, R.drawable.white_dog, R.drawable.white_fish, R.drawable.white_squirrel, R.drawable.white_turtle,
			R.drawable.white_penguin, R.drawable.white_footprint, R.drawable.white_foot_print, R.drawable.white_flower, R.drawable.white_pine_tree, R.drawable.white_palm_tree,
			R.drawable.white_fire, R.drawable.white_folder_sharing, R.drawable.white_fuel_station, R.drawable.white_gift, R.drawable.white_globe, R.drawable.white_graph,
			R.drawable.white_hand, R.drawable.white_heart, R.drawable.white_help, R.drawable.white_hour_glass, R.drawable.white_house, R.drawable.white_houses,
			R.drawable.white_office, R.drawable.white_university, R.drawable.white_tent, R.drawable.white_sofa, R.drawable.white_identity_card,
			R.drawable.white_instrument_barometer, R.drawable.white_instrument_round_bottom_flask, R.drawable.white_instrument_telescope, R.drawable.white_iris,
			R.drawable.white_solar, R.drawable.white_moon, R.drawable.white_energy_saving_bulb_1, R.drawable.white_energy_saving_bulb_2, R.drawable.white_light,
			R.drawable.white_light_bulb, R.drawable.white_torch, R.drawable.white_linear_gauge, R.drawable.white_driller, R.drawable.white_key_access, R.drawable.white_lock,
			R.drawable.white_latitude, R.drawable.white_magnet, R.drawable.white_map_location, R.drawable.white_media_back, R.drawable.white_media_end,
			R.drawable.white_media_fast_forward, R.drawable.white_media_pause, R.drawable.white_media_play, R.drawable.white_messages, R.drawable.white_plus,
			R.drawable.white_minus, R.drawable.white_movie, R.drawable.white_new, R.drawable.white_next, R.drawable.white_outlet_plug, R.drawable.white_phone,
			R.drawable.white_photograph, R.drawable.white_picture, R.drawable.white_picture_frame, R.drawable.white_planet, R.drawable.white_plug, R.drawable.white_power_1,
			R.drawable.white_power_2, R.drawable.white_printer, R.drawable.white_printer_2, R.drawable.white_puzzles, R.drawable.white_left_arrow, R.drawable.white_reload,
			R.drawable.white_road_backward, R.drawable.white_road_forward, R.drawable.white_circle, R.drawable.white_rectangle, R.drawable.white_shape12, R.drawable.white_shape6,
			R.drawable.white_shape_cube, R.drawable.white_shape_peace, R.drawable.white_shape_spade, R.drawable.white_shape_square, R.drawable.white_shape_square2,
			R.drawable.white_shape_star, R.drawable.white_shape_triangle, R.drawable.white_tube, R.drawable.white_smile, R.drawable.white_laugh, R.drawable.white_sad,
			R.drawable.white_sleepy, R.drawable.white_smoking_zone, R.drawable.white_check, R.drawable.white_close, R.drawable.white_synchronize, R.drawable.white_tag,
			R.drawable.white_target, R.drawable.white_toolbox, R.drawable.white_tools, R.drawable.white_headphone, R.drawable.white_gramaphone, R.drawable.white_guitar,
			R.drawable.white_recorded_media, R.drawable.white_beamed_notes, R.drawable.white_quarter_note, R.drawable.white_microphone, R.drawable.white_tape_recorder,
			R.drawable.white_loud_speaker, R.drawable.white_voicemail, R.drawable.white_speaker_volume, R.drawable.white_speech, R.drawable.white_volume_mute,
			R.drawable.white_volume_high, R.drawable.white_volume_low, R.drawable.white_announcement, R.drawable.white_waiting_popup, R.drawable.white_warning_message,
			R.drawable.white_refrigerator, R.drawable.white_washing_machine, R.drawable.white_internet, R.drawable.white_bluetooth, R.drawable.white_beep_signal,
			R.drawable.white_network_sharing, R.drawable.white_lan, R.drawable.white_microsoft_windows, R.drawable.white_windows_pc, R.drawable.white_linux,
			R.drawable.white_mac_pc, R.drawable.white_command_mac, R.drawable.white_laptop, R.drawable.white_ipad, R.drawable.white_mobile_phone, R.drawable.white_telephone,
			R.drawable.white_webcam, R.drawable.white_usb, R.drawable.white_mail, R.drawable.white_satellite, R.drawable.white_cd, R.drawable.white_dvd, R.drawable.white_keyboard,
			R.drawable.white_mouse, R.drawable.white_joystick, R.drawable.white_film, R.drawable.white_feather, R.drawable.white_water_tap, R.drawable.white_weather,
			R.drawable.white_umbrella, R.drawable.white_thunder, R.drawable.white_sports, R.drawable.white_bat_man, R.drawable.white_armor, R.drawable.white_sword,
			R.drawable.white_attachment, R.drawable.white_balloon, R.drawable.white_search, R.drawable.white_settings, R.drawable.white_yin_yang, R.drawable.white_cooker,
			R.drawable.white_cutlery_fork_knife, R.drawable.white_snack_doughnut, R.drawable.white_beverage_coffee, R.drawable.white_cake, R.drawable.white_cheese,
			R.drawable.white_chef_cap, R.drawable.white_bell, R.drawable.white_book_open, R.drawable.white_danger, R.drawable.white_delete, R.drawable.white_finger_print,
			R.drawable.white_floppy, R.drawable.white_hat, R.drawable.white_knight, R.drawable.white_link, R.drawable.white_logout, R.drawable.white_password,
			R.drawable.white_shape_lightning, R.drawable.white_button, R.drawable.white_swap_down, R.drawable.white_swap_left, R.drawable.white_swap_right,
			R.drawable.white_swap_up, R.drawable.white_table_fan, R.drawable.white_video_camera, R.drawable.white_table_lamp, R.drawable.white_remote_control,
			R.drawable.white_television, R.drawable.white_toy };

	/**
	 * Creates the icon selection dialog.
	 * 
	 * @param context
	 *            The context
	 * @param listener
	 *            Used as callback when the user selects an icon.
	 */
	public IconSelector(Context context, final OnIconSelectedListener listener) {
		super(context);

		setContentView(R.layout.dialog_icon_selector);
		setTitle(R.string.choose_icon);
		GridView grid = (GridView) findViewById(R.id.icon_selector_grid);
		grid.setAdapter(new IconAdapter());

		grid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				IconSelector.this.dismiss();
				listener.onIconSelected(getContext().getResources().getResourceEntryName(ICONS[position]));
			}

		});

		setCancelable(true);
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	private class IconAdapter extends BaseAdapter {

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i = new ImageView(getContext());
			i.setImageResource(ICONS[position]);
			i.setScaleType(ImageView.ScaleType.FIT_CENTER);
			final int w = (int) (16 * getContext().getResources().getDisplayMetrics().density + 0.5f);
			i.setLayoutParams(new GridView.LayoutParams(w * 2, w * 2));
			i.setPadding(5, 5, 5, 5);

			if (getContext().getResources().getResourceEntryName(ICONS[position]).startsWith("white")) {
				i.setBackgroundColor(0xFF000000);
			}

			return i;
		}

		public final int getCount() {
			return ICONS.length;
		}

		public final long getItemId(int position) {
			return position;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}
	}

}
