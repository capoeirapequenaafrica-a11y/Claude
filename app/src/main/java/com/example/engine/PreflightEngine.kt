package com.example.engine

import com.example.data.model.BugItem
import com.example.data.model.ContrastCheckResult
import com.example.data.model.LintIssue
import com.example.data.model.PreflightItem
import com.example.data.model.PreflightStatus
import com.example.data.model.SweepHit

object PreflightEngine {
    fun evaluatePreflight(
        parityResult: ParityResult,
        bugs: List<BugItem>,
        contrastResults: List<ContrastCheckResult>,
        sweepHits: List<SweepHit>,
        listingIssues: List<LintIssue>
    ): PreflightStatus {
        val checklist = mutableListOf<PreflightItem>()

        // 1. Must-have features parity
        val mustHavesPassed = parityResult.mustHaveDone >= parityResult.mustHaveTotal && parityResult.mustHaveTotal > 0
        checklist.add(
            PreflightItem(
                title = "Paridade Obrigatória (Must-Have)",
                description = "${parityResult.mustHaveDone} de ${parityResult.mustHaveTotal} recursos obrigatórios implementados (100% necessário)",
                passed = mustHavesPassed
            )
        )

        // 2. Parity Score threshold (>= 80%)
        val parityThresholdPassed = parityResult.featureScore >= 80.0
        checklist.add(
            PreflightItem(
                title = "Pontuação de Paridade >= 80%",
                description = "Pontuação atual: ${parityResult.featureScore}%",
                passed = parityThresholdPassed
            )
        )

        // 3. Open S1/S2 Blocker & Critical bugs
        val openBlockers = bugs.count { !it.isResolved && (it.severity == "S1" || it.severity == "S2") }
        val bugsPassed = openBlockers == 0
        checklist.add(
            PreflightItem(
                title = "Zero Bugs Críticos S1 / S2",
                description = if (bugsPassed) "Todas as jornadas críticas de teste verificadas" else "$openBlockers bug(s) S1/S2 não resolvidos bloqueando o lançamento",
                passed = bugsPassed
            )
        )

        // 4. Accessibility WCAG AA Contrast
        val failedTokens = contrastResults.count { !it.passAa }
        val contrastPassed = failedTokens == 0
        checklist.add(
            PreflightItem(
                title = "Acessibilidade e Contraste WCAG 2.2 AA",
                description = if (contrastPassed) "Todos os tokens de design atendem à proporção AA" else "$failedTokens par(es) de cores falham no contraste AA",
                passed = contrastPassed
            )
        )

        // 5. Clean-Room Brand Sweep
        val sweepClean = sweepHits.isEmpty()
        checklist.add(
            PreflightItem(
                title = "Varredura Limpa de Marcas (Sweep)",
                description = if (sweepClean) "Zero marcas, domínios ou cores do concorrente detectados" else "${sweepHits.size} vazamento(s) de marca do concorrente detectado(s)",
                passed = sweepClean
            )
        )

        // 6. App Store & Google Play Listing
        val listingErrors = listingIssues.count { it.level == "error" }
        val listingValid = listingErrors == 0
        checklist.add(
            PreflightItem(
                title = "Metadados da Loja & Conformidade",
                description = if (listingValid) "Metadados aprovados nos limites de caracteres e regras das lojas" else "$listingErrors erro(s) de validação na listagem da loja",
                passed = listingValid
            )
        )

        val allPassed = mustHavesPassed && parityThresholdPassed && bugsPassed && contrastPassed && sweepClean && listingValid

        return PreflightStatus(
            passed = allPassed,
            parityDone = mustHavesPassed,
            parityScore = parityResult.featureScore,
            mustHavesDone = parityResult.mustHaveDone,
            mustHavesTotal = parityResult.mustHaveTotal,
            openBlockers = openBlockers,
            contrastPassed = contrastPassed,
            sweepClean = sweepClean,
            listingValid = listingValid,
            checklist = checklist
        )
    }
}
