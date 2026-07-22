package site.siredvin.peripheralworks.integrations.emi

class Integration : Runnable {
    override fun run() {
        CommonEntrypoint.init()
    }
}
