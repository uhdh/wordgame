package com.gamehub.wordgame.net

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

/**
 * "단어조각" 전용 Supabase 테이블 (`word_fragments_leaderboard`). 웹/다른 미니게임이
 * 공유하는 `card_chess_leaderboard`와는 완전히 분리된 별도 테이블이다.
 */
object LeaderboardApi {
    private const val SUPABASE_URL = "https://paktzmofotvwfdxcpmzv.supabase.co"
    private const val SUPABASE_ANON_KEY = "sb_publishable_jWbstEn2pKJTNDxLTR4Jig_asglvzGW"
    private const val TABLE = "word_fragments_leaderboard"

    private val client = OkHttpClient()
    private val jsonMedia = "application/json".toMediaType()

    data class RankRow(val id: String, val nickname: String, val score: Int, val cleared: Int)

    private fun Request.Builder.withAuth(): Request.Builder = this
        .header("apikey", SUPABASE_ANON_KEY)
        .header("Authorization", "Bearer $SUPABASE_ANON_KEY")

    suspend fun fetchTop(limit: Int = 20): List<RankRow> = withContext(Dispatchers.IO) {
        val url = "$SUPABASE_URL/rest/v1/$TABLE" +
            "?select=id,nickname,score,cleared,updated_at" +
            "&order=score.desc,updated_at.asc&limit=$limit"
        val req = Request.Builder().url(url).withAuth().get().build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw java.io.IOException("리더보드를 불러오지 못했습니다")
            val arr = JSONArray(resp.body?.string().orEmpty())
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                RankRow(
                    id = o.optString("id"),
                    nickname = o.optString("nickname"),
                    score = o.optInt("score", 0),
                    cleared = o.optInt("cleared", 0)
                )
            }
        }
    }

    suspend fun submitScore(nickname: String, score: Int, clearedStagesCount: Int) = withContext(Dispatchers.IO) {
        val findUrl = "$SUPABASE_URL/rest/v1/$TABLE?nickname=eq." +
            java.net.URLEncoder.encode(nickname, "UTF-8")
        val findReq = Request.Builder().url(findUrl).withAuth().get().build()
        val existing = client.newCall(findReq).execute().use { resp ->
            JSONArray(resp.body?.string().orEmpty())
        }

        if (existing.length() > 0) {
            val cur = existing.getJSONObject(0)
            val curScore = cur.optInt("score", 0)
            val curCleared = cur.optInt("cleared", 0)
            if (score > curScore || clearedStagesCount > curCleared) {
                val body = JSONObject().apply {
                    put("score", maxOf(score, curScore))
                    put("cleared", maxOf(clearedStagesCount, curCleared))
                    put("updated_at", Instant.now().toString())
                }.toString().toRequestBody(jsonMedia)
                val patchReq = Request.Builder()
                    .url("$SUPABASE_URL/rest/v1/$TABLE?id=eq.${cur.optString("id")}")
                    .withAuth()
                    .patch(body)
                    .build()
                client.newCall(patchReq).execute().use { resp ->
                    if (!resp.isSuccessful) throw java.io.IOException("점수 갱신 실패")
                }
            }
        } else {
            val body = JSONObject().apply {
                put("nickname", nickname)
                put("score", score)
                put("cleared", clearedStagesCount)
            }.toString().toRequestBody(jsonMedia)
            val postReq = Request.Builder()
                .url("$SUPABASE_URL/rest/v1/$TABLE")
                .withAuth()
                .header("Prefer", "return=minimal")
                .post(body)
                .build()
            client.newCall(postReq).execute().use { resp ->
                if (!resp.isSuccessful) throw java.io.IOException("점수 등록 실패")
            }
        }
    }
}
