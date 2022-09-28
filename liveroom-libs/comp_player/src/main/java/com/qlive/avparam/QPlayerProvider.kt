package com.qlive.avparam

interface QPlayerProvider {
    var playerGetter: (() -> QIPlayer)
}