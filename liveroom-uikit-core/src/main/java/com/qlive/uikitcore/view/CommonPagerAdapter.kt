package com.qlive.uikitcore.view;

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter

class CommonPagerAdapter(private val fragmentsList: List<Fragment>, mFm: FragmentManager, var titles:List<String>?=null) : FragmentStatePagerAdapter(mFm) {
    override fun getCount(): Int {
        return fragmentsList.size
    }

    override fun getItem(arg0: Int): Fragment {
        return fragmentsList[arg0]
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun getPageTitle(position: Int): CharSequence? {
        titles?.let {
            return it[position]
        }
        return super.getPageTitle(position)
    }
}

class CommonViewPagerAdapter(private val viewLists: List<View>) :PagerAdapter(){

    override fun getCount(): Int {
        return viewLists.size
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(viewLists[position]);
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.addView(viewLists[position]);
        return viewLists[position];
    }
}