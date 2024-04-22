/*
 * Copyright: (c)  2023-2024, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.cli

import com.e13mort.palantir.render.ReportRender

object DoneOperationRenderer : ReportRender<Unit, String, Unit> {
    override fun render(value: Unit, params: Unit): String {
        return "Done"
    }
}