package com.app.fragment;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.app.R;

import java.util.zip.Inflater;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

	private Fragment mCurrentFragment;

	public SectionsPagerAdapter(FragmentManager fm, int behavior) {
		super(fm, behavior);
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

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object){
		if(mCurrentFragment != object){
			mCurrentFragment = (Fragment) object;
		}
		super.setPrimaryItem(container, position, object);
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

	public Fragment getCurrentFragment(){
		return mCurrentFragment;
	}
}