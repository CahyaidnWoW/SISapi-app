<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Services\HealthRecordService;
use Illuminate\Http\Request;
use Exception;

class HealthRecordController extends Controller
{
    protected HealthRecordService $healthRecordService;

    public function __construct(HealthRecordService $healthRecordService)
    {
        $this->healthRecordService = $healthRecordService;
    }

    public function store(Request $request, string $tagNumber)
    {
        $validated = $request->validate([
            'type' => 'required|in:vaccination,treatment,emergency_report',
            'diagnosis' => 'nullable|string',
            'treatment' => 'nullable|string',
            'next_due_date' => 'nullable|date',
            'date' => 'required|date',
            'photo' => 'nullable|image|max:5120', 
        ]);

        try {
            $record = $this->healthRecordService->create(
                $tagNumber,
                $validated,
                $request->file('photo'),
                $request->user()->id
            );

            return response()->json($record, 201);
        } catch (Exception $e) {
            return response()->json(['message' => $e->getMessage()], 404);
        }
    }

    public function update(Request $request, int $id)
    {
        $validated = $request->validate([
            'diagnosis' => 'nullable|string',
            'treatment' => 'nullable|string',
        ]);

        try {
            $record = $this->healthRecordService->update($id, $validated, $request->user()->id);

            return response()->json($record);
        } catch (Exception $e) {
            return response()->json(['message' => $e->getMessage()], 404);
        }
    }

    public function byLivestock(string $tagNumber)
    {
        try {
            $records = $this->healthRecordService->getByLivestock($tagNumber);

            return response()->json($records);
        } catch (Exception $e) {
            return response()->json(['message' => $e->getMessage()], 404);
        }
    }

    public function due()
    {
        return response()->json($this->healthRecordService->getDueVaccinations());
    }

    public function emergency()
    {
    return response()->json($this->healthRecordService->getEmergencyReports());
    }

    public function photo(int $id)
    {
        $record = \App\Models\HealthRecord::find($id);

        if (!$record || !$record->photo_path) {
            return response()->json(['message' => 'Foto tidak ditemukan'], 404);
        }

        $filePath = storage_path('app/public/' . $record->photo_path);

        if (!file_exists($filePath)) {
            return response()->json(['message' => 'File fisik foto tidak ditemukan di server'], 404);
        }

        return response()->file($filePath);
    }

}