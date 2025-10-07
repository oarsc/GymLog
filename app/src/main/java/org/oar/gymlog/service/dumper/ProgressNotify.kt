package org.oar.gymlog.service.dumper

typealias ProgressNotify = (Int, String) -> Unit
val EMPTY: ProgressNotify = { _, _ ->}
