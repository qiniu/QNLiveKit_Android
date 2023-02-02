package com.qlive.uikitktv

import android.view.Gravity
import android.widget.SeekBar
import com.qlive.ktvservice.QKTVMusic.track_accompany
import com.qlive.ktvservice.QKTVMusic.track_originVoice
import com.qlive.ktvservice.QKTVService
import com.qlive.pushclient.QPusherClient
import com.qlive.uikitcore.dialog.ViewBindingDialogFragment
import com.qlive.uikitktv.databinding.KitDialogMusicSettingBinding

class MusicSettingDialog(val client: QPusherClient, val ktvService: QKTVService) :
    ViewBindingDialogFragment<KitDialogMusicSettingBinding>() {

    init {
        applyGravityStyle(Gravity.BOTTOM)
        applyDimAmount(0f)
    }

    override fun init() {
        binding.sbVol1.progress = (client.microphoneVolume * 100).toInt()
        binding.sbVol2.progress = (ktvService.musicVolume * 100).toInt()
        binding.sbVol1.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                client.microphoneVolume = (p1 / 100.0).toDouble()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
        binding.sbVol2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                ktvService.musicVolume = ((p1 / 100.0).toFloat())
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
        binding.switchEar.isChecked = ktvService.currentMusic.track == track_originVoice

        binding.switchEar.setOnCheckedChangeListener { _, b ->
            if (b) {
                ktvService.switchTrack(track_originVoice)
            } else {
                ktvService.switchTrack(track_accompany)
            }
        }

        binding.switchEar2.setOnCheckedChangeListener { _, b ->
            if (b) {
                client.enableEarMonitor(true)
            } else {
                client.enableEarMonitor(false)
            }
        }
    }
}