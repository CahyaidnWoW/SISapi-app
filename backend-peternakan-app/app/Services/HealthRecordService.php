<?php

namespace App\Services;

use App\Models\HealthRecord;
use App\Models\Livestock;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\Storage;
use Exception;

class HealthRecordService
{
    public function create(string $tagNumber, array $data, ?UploadedFile $photo, int $userId)
    {
        $livestock = Livestock::where('tag_number', $tagNumber)->first();

        if (!$livestock) {
            throw new Exception("Sapi dengan tag_number {$tagNumber} tidak ditemukan");
        }

        $photoPath = null;
        if ($photo) {
            $photoPath = $photo->store('health-reports', 'public');
        }

        return HealthRecord::create([
            'livestock_id' => $livestock->id,
            'type' => $data['type'],
            'diagnosis' => $data['diagnosis'] ?? null,
            'treatment' => $data['treatment'] ?? null,
            'photo_path' => $photoPath,
            'next_due_date' => $data['next_due_date'] ?? null,
            'handled_by' => $userId,
            'date' => $data['date'],
        ]);
    }

    public function update(int $id, array $data, int $userId)
    {
        $record = HealthRecord::find($id);

        if (!$record) {
            throw new Exception("Rekam medis tidak ditemukan");
        }

        $record->update([
            'diagnosis' => $data['diagnosis'] ?? $record->diagnosis,
            'treatment' => $data['treatment'] ?? $record->treatment,
            'handled_by' => $userId,
        ]);

        return $record;
    }

    public function getByLivestock(string $tagNumber)
    {
        $livestock = Livestock::where('tag_number', $tagNumber)->first();

        if (!$livestock) {
            throw new Exception("Sapi dengan tag_number {$tagNumber} tidak ditemukan");
        }

        return $livestock->healthRecords()->orderByDesc('date')->get();
    }

    public function getDueVaccinations()
    {
        return HealthRecord::where('type', 'vaccination')
            ->whereNotNull('next_due_date')
            ->where('next_due_date', '<=', now()->addDays(7)) // tampil H-7 sebelum jatuh tempo
            ->with('livestock')
            ->orderBy('next_due_date')
            ->get();
    }
}