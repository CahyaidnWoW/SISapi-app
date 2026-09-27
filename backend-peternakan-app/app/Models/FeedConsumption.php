<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class FeedConsumption extends Model
{
    protected $guarded = ['id'];

    public function cage() {
        return $this->belongsTo(Cage::class);
    }

    public function feedStock() {
        return $this->belongsTo(FeedStock::class, 'feed_id');
    }

    public function worker() {
        return $this->belongsTo(User::class, 'worker_id');
    }
}