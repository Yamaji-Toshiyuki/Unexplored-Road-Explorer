package com.app.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

	public SectionsPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@NonNull
	@Override
	public Fragment getItem(int position) {
		// getItem is called to instantiate the fragment for the given page.
		switch(position){
			case 0:
				return OSMFragment.newInstance();
			case 1:
				return SearchFragment.newInstance();
			case 2:
				return GalleryFragment.newInstance();
			default:
				return null;
		}
	}

	@Nullable
	@Override
	public CharSequence getPageTitle(int position) {
		return "";
	}

	@Override
	public int getCount() {
		// Show 3 total pages.
		return 3;
	}
}