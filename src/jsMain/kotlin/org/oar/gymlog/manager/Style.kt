package org.oar.gymlog.manager

import org.oar.gymlog.manager.custom.style

object Style {
    fun load() {
        style {
            "body" {
                "margin" to "0"
                "font-family" to "sans"
                "min-width" to "800px"
            }

            "input[type=date], input[type=datetime-local]" {
                "height" to "19px"
            }

            "input.subtle, select.subtle" {
                "&:not(:focus)" {
                    "background-color" to "transparent"
                    "border" to "1px solid transparent"
                }

                "&:hover:not(:disabled)" {
                    "border-color" to "#5f5f68"
                }
            }

            "button.$BUTTON_STYLE" {
                "margin" to "0px 2px"
                "padding" to "2px 10px"
                "font-size" to "0.85em"
                "background-color" to "#a1c6ff"
                "border" to "1px solid #658dcb"
                "cursor" to "pointer"
                "transition" to "background-color .3s"

                "&.big" {
                    "padding" to "8px 20px"
                }

                "&:hover:not(:disabled)" {
                    "background-color" to "#7ca6e8"
                }
                "&:disabled" {
                    "opacity" to "0.6"
                    "cursor" to "initial"
                }

                "&.red" {
                    "background-color" to "#ffc2bb"
                    "border-color" to "#e28279"

                    "&:hover:not(:disabled)" {
                        "background-color" to "#ff9c93"
                    }
                }

                "&.green" {
                    "background-color" to "#a4e89c"
                    "border-color" to "#60ac59"

                    "&:hover:not(:disabled)" {
                        "background-color" to "#78c870"
                    }
                }

                "&.transparent" {
                    "background-color" to "#0000"
                    "border-color" to "#0000"

                    "&:hover:not(:disabled)" {
                        "background-color" to "#00000017"
                    }
                }
            }
        }
    }

    const val BUTTON_STYLE = "o-button"
}