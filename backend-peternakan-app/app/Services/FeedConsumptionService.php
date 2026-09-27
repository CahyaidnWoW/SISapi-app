<?php

namespace App\Services;

use App\Models\FeedConsumption;
use App\Models\FeedStockBatch;
use Illuminate\Support\Facades\DB;
use Exception;

class FeedConsumptionService
{
    public function record(array $data, int $workerId)
    {
        return DB::transaction(function () use ($data, $workerId) {
            $amountNeeded = $data['amount_used'];

            $batches = FeedStockBatch::where('feed_id', $data['feed_id'])
                ->where('quantity_remaining', '>', 0)
                ->orderBy('entry_date', 'asc')
                ->lockForUpdate()
                ->get();

            $totalAvailable = $batches->sum('quantity_remaining');

            if ($totalAvailable < $amountNeeded) {
                throw new Exception("Stok pakan tidak mencukupi. Tersedia: {$totalAvailable}, dibutuhkan: {$amountNeeded}");
            }

            foreach ($batches as $batch) {
                if ($amountNeeded <= 0) {
                    break;
                }

                $deduct = min($batch->quantity_remaining, $amountNeeded);
                $batch->quantity_remaining -= $deduct;
                $batch->save();

                $amountNeeded -= $deduct;
            }
            
            $consumption = FeedConsumption::create([
                'cage_id' => $data['cage_id'],
                'feed_id' => $data['feed_id'],
                'amount_used' => $data['amount_used'],
                'date' => $data['date'],
                'worker_id' => $workerId,
            ]);

            return $consumption;
        });
    }
}