package com.example.smile.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

@SuppressLint("NewApi")
object NetworkUtils {
    private const val TAG = "ConnectManager"

    /** Indicates this network uses a Cellular transport. */
    const val TRANSPORT_CELLULAR = 0

    /** Indicates this network uses a Wi-Fi transport. */
    const val TRANSPORT_WIFI = 1

    /** Indicates this network uses a Bluetooth transport. */
    const val TRANSPORT_BLUETOOTH = 2

    /** Indicates this network uses an Ethernet transport. */
    const val TRANSPORT_ETHERNET = 3

    /** Indicates this network uses a VPN transport. */
    const val TRANSPORT_VPN = 4

    /** Indicates this network uses a Wi-Fi Aware transport. */
    const val TRANSPORT_WIFI_AWARE = 5

    /** Indicates this network uses a LoWPAN transport. */
    const val TRANSPORT_LOWPAN = 6

    /**
     * Indicates this network uses a Test-only virtual interface as a
     * transport.
     *
     * @hide
     */
    const val TRANSPORT_TEST = 7

    /** Indicates this network uses a USB transport. */
    const val TRANSPORT_USB = 8

    /**
     * >= Android 10（Q版本）推荐
     *
     * 当前使用MOBILE流量上网
     */
    fun isMobileNetwork(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
    }

    /**
     * >= Android 10（Q版本）推荐
     *
     * 当前使用WIFI上网
     */
    fun isWifiNetwork(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
    }

    /**
     * >= Android 10（Q版本）推荐
     *
     * 当前使用以太网上网
     */
    fun isEthernetNetwork(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
    }

    /**
     * >= Android 10（Q版本）推荐
     *
     * NetworkCapabilities.NET_CAPABILITY_INTERNET，表示此网络应该(maybe)能够访问internet
     *
     * 判断当前网络可以正常上网 表示此连接此网络并且能成功上网。
     * 例如，对于具有NET_CAPABILITY_INTERNET的网络，这意味着已成功检测到INTERNET连接。
     */
    fun isConnectedAvailableNetwork(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
    }

    /**
     * >= Android 10（Q版本）推荐
     *
     * 获取成功上网的网络类型 value = { TRANSPORT_CELLULAR, 0 表示此网络使用蜂窝传输。
     * TRANSPORT_WIFI, 1 表示此网络使用Wi-Fi传输。 TRANSPORT_BLUETOOTH, 2 表示此网络使用蓝牙传输。
     * TRANSPORT_ETHERNET, 3 表示此网络使用以太网传输。 TRANSPORT_VPN, 4 表示此网络使用VPN传输。
     * TRANSPORT_WIFI_AWARE, 5 表示此网络使用Wi-Fi感知传输。 TRANSPORT_LOWPAN, 6
     * 表示此网络使用LoWPAN传输。 TRANSPORT_TEST, 7 指示此网络使用仅限测试的虚拟接口作为传输。 TRANSPORT_USB,
     * 8 表示此网络使用USB传输 }
     */
    fun getConnectedNetworkType(context: Context): Int {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return -1
        val capabilities = cm.getNetworkCapabilities(network) ?: return -1
        if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    return NetworkCapabilities.TRANSPORT_CELLULAR
                }

                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    return NetworkCapabilities.TRANSPORT_WIFI
                }

                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> {
                    return NetworkCapabilities.TRANSPORT_BLUETOOTH
                }

                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    return NetworkCapabilities.TRANSPORT_ETHERNET
                }

                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                    return NetworkCapabilities.TRANSPORT_VPN
                }

                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE) -> {
                    return NetworkCapabilities.TRANSPORT_WIFI_AWARE
                }

                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN) -> {
                    return NetworkCapabilities.TRANSPORT_LOWPAN
                }

                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_USB) -> {
                    return NetworkCapabilities.TRANSPORT_USB
                }
            }
        }
        return -1
    }
}