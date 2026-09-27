<?php
namespace App\Services;

use App\Models\Cage;
use App\Models\FeedConsumption;
use App\Models\Livestock;

class FcrService
{
    public function calculateCageFcr(int $cageId): array
    {
        $cage = Cage::findOrFail($cageId);
        $totalFeed = FeedConsumption::where('cage_id', $cageId)->sum('amount_used');
        $livestocks = Livestock::where('cage_id', $cageId)->get();

        $totalWeightGain = 0;

        foreach ($livestocks as $livestock) {
            $latestLog = $livestock->weightLogs()->orderBy('date', 'desc')->orderBy('id', 'desc')->first();
            $currentWeight = $latestLog ? $latestLog->weight : $livestock->initial_weight;

            $gain = $currentWeight - $livestock->initial_weight;
            if ($gain > 0) {
                $totalWeightGain += $gain;
            }
        }

        $fcr = $totalWeightGain > 0 ? round($totalFeed / $totalWeightGain, 2) : 0;

        return [
            'cage_id'                => $cage->id,
            'cage_name'              => $cage->name,
            'total_livestocks'       => $livestocks->count(),
            'total_feed_consumed_kg' => (float) $totalFeed,
            'total_weight_gained_kg' => (float) $totalWeightGain,
            'fcr'                    => $fcr,
        ];
    }
}