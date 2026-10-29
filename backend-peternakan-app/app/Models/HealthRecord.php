<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class HealthRecord extends Model
{
    protected $guarded = ['id'];
    protected $appends = ['photo_url'];

    public function getPhotoUrlAttribute()
    {
        return $this->photo_path ? url('api/health-records/' . $this->id . '/photo') : null;
    }


    public function livestock() {
        return $this->belongsTo(Livestock::class);
    }

    public function handler() {
        return $this->belongsTo(User::class, 'handled_by');
    }
}
