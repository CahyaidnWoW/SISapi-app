<?php
namespace App\Services;

use App\Models\Livestock;
use App\Models\WeightLog;

class WeightLogService
{
   public function recordWeightByTag(string $tagNumber, array $data, int $userId): WeightLog
{
    $livestock = Livestock::where('tag_number', $tagNumber)->firstOrFail();

    $data['livestock_id'] = $livestock->id;
    $data['worker_id']    = $userId;

    return WeightLog::create($data);
}
}
?>